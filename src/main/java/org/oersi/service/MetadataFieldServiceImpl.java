package org.oersi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.oersi.config.BaseFieldConfig;
import org.oersi.domain.BackendMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.oersi.domain.BackendMetadata.mapToElasticsearchPath;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataFieldServiceImpl implements MetadataFieldService {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final @NonNull BaseFieldConfig baseFieldConfig;

  @Value("classpath:backend-index-mapping.json")
  private Resource indexMapping;

  @Override
  public BackendMetadata toMetadata(Map<String, Object> properties) {
    return toMetadata(properties, baseFieldConfig.getResourceIdentifier());
  }

  @Override
  public String getIdentifier(BackendMetadata metadata) {
    return getIdentifier(metadata.getData(), baseFieldConfig.getResourceIdentifier());
  }

  private static String getIdentifier(Map<String, Object> properties, String resourceIdentifierField) {
    List<String> ids = getValuesFromObject(properties, resourceIdentifierField);
    if (ids.isEmpty()) {
      throw new IllegalArgumentException("Resource identifier is missing");
    }
    return ids.get(0);
  }

  public static BackendMetadata toMetadata(Map<String, Object> properties, String resourceIdentifierField) {
    String id = getIdentifier(properties, resourceIdentifierField);
    var base64Id = Base64.getUrlEncoder().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    BackendMetadata result = new BackendMetadata();
    result.setData(properties);
    result.setId(base64Id);
    return result;
  }

  @Override
  public Document getBackendMetadataMapping() {
    Document mapping;
    try {
      mapping = Document.parse(IOUtils.toString(indexMapping.getInputStream(), Charset.defaultCharset()));
      if (useMetadataSource()) {
        Set<String> queryFields = new HashSet<>();
        queryFields.add(getMetadataSourceIdentifierField());
        if (baseFieldConfig.getMetadataSource().getQueries() != null) {
          queryFields.addAll(baseFieldConfig.getMetadataSource().getQueries().stream().map(q -> getFullMetadataSourceFieldPath(q.getField())).toList());
        }
        for (String field : queryFields) {
          mapping = addFieldToMapping(mapping, mapToElasticsearchPath(field));
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("index mapping cannot be loaded");
    }
    return mapping;
  }

  @Override
  public String getNamedMetadataSourceQueryField(String queryName) {
    BaseFieldConfig.MetadataSource.Query query = getQuery(queryName);
    return query == null ? "" : getFullMetadataSourceFieldPath(query.getField());
  }

  @Override
  public String getMetadataSourceIdentifierField() {
    if (useMetadataSource()) {
      if (baseFieldConfig.getMetadataSource().isObject()) {
        return getFullMetadataSourceFieldPath(baseFieldConfig.getMetadataSource().getObjectIdentifier());
      }
      return baseFieldConfig.getMetadataSource().getField();
    }
    return "";
  }

  @Override
  public List<MetadataSourceItem> getMetadataSourceItems(Map<String, Object> data) {
    if (useMetadataSource()) {
      if (baseFieldConfig.getMetadataSource().isObject()) {
        List<Map<String, Object>> items = getValuesFromObject(data, baseFieldConfig.getMetadataSource().getField());
        return items.stream().map(MetadataSourceItem::new).toList();
      }
      List<String> values = getValuesFromObject(data, baseFieldConfig.getMetadataSource().getField());
      return values.stream().map(MetadataSourceItem::new).toList();
    }
    return new ArrayList<>();
  }

  @Override
  public List<String> getValues(MetadataSourceItem metadataSourceItem, String metadataSourceField) {
    if (baseFieldConfig.getMetadataSource().isObject()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> object = (Map<String, Object>) metadataSourceItem.getValue();
      return getValuesFromObject(object, getMetadataSourceSubField(metadataSourceField));
    } else if (baseFieldConfig.getMetadataSource().getField().equals(metadataSourceField)) {
      return List.of(metadataSourceItem.getValue().toString());
    }
    return new ArrayList<>();
  }

  @Override
  public String getIdentifier(MetadataSourceItem metadataSourceItem) {
    String identifierField = getMetadataSourceIdentifierField();
    List<String> values = getValues(metadataSourceItem, identifierField);
    return values.isEmpty() ? "" : values.get(0);
  }

  @Override
  public void updateMetadataSource(Map<String, Object> data, List<MetadataSourceItem> metadataSourceItems) {
    if (data != null) {
      String metadataSourceField = baseFieldConfig.getMetadataSource().getField();
      Map<String, Object> metadataSourceParent = data;
      if (metadataSourceField.contains(".")) {
        String metadataSourceParentField = metadataSourceField.substring(0, metadataSourceField.lastIndexOf("."));
        metadataSourceField = metadataSourceField.substring(metadataSourceField.lastIndexOf(".") + 1);
        for (String field: metadataSourceParentField.split("\\.")) {
          metadataSourceParent = addOrGetObject(metadataSourceParent, field);
        }
      }
      if (baseFieldConfig.getMetadataSource().useMultipleItems()) {
        metadataSourceParent.put(metadataSourceField, metadataSourceItems.stream().map(MetadataSourceItem::getValue).toList());
      } else if (metadataSourceItems.isEmpty()) {
        metadataSourceParent.remove(metadataSourceField);
      } else {
        MetadataSourceItem item = metadataSourceItems.get(0);
        metadataSourceParent.put(metadataSourceField, item.getValue());
      }
    }
  }

  private boolean useMetadataSource() {
    return baseFieldConfig.getMetadataSource() != null;
  }

  private String getFullMetadataSourceFieldPath(String subFieldName) {
    return baseFieldConfig.getMetadataSource().getField() + "." + subFieldName;
  }

  private String getMetadataSourceSubField(String fullFieldPath) {
    if (fullFieldPath.startsWith(baseFieldConfig.getMetadataSource().getField()+ ".")) {
      return fullFieldPath.substring(baseFieldConfig.getMetadataSource().getField().length() + 1);
    }
    throw new IllegalArgumentException("fullFieldPath does not match metadataSourceField");
  }

  private BaseFieldConfig.MetadataSource.Query getQuery(String queryName) {
    if (baseFieldConfig.getMetadataSource().getQueries() != null) {
      for (BaseFieldConfig.MetadataSource.Query query : baseFieldConfig.getMetadataSource().getQueries()) {
        if (query.getName().equals(queryName)) {
          return query;
        }
      }
    }
    return null;
  }

  /**
   * @param mapping        the mapping to which to the new field is to be added
   * @param compositeField the new field to be added; complete path, subfields separated by dot '.'
   * @return the extended mapping
   */
  private static Document addFieldToMapping(Map<String, Object> mapping, String compositeField) {
    List<String> fields = Arrays.asList(compositeField.split("\\."));
    Iterator<String> fieldIterator = fields.iterator();
    Map<String, Object> item = mapping;
    while (fieldIterator.hasNext()) {
      String field = fieldIterator.next();
      item = addOrGetObject(item, "properties");
      item = addOrGetObject(item, field);
      if (!fieldIterator.hasNext()) {
        item.put("type", "keyword");
      }
    }
    return Document.from(mapping);
  }

  private static Map<String, Object> addOrGetObject(Map<String, Object> parentObject, String fieldName) {
    Object value = parentObject.computeIfAbsent(fieldName, k -> new HashMap<String, Object>());
    Map<String, Object> child = objectMapper.convertValue(value, new TypeReference<>() {
    });
    parentObject.put(fieldName, child);
    return child;
  }

  /**
   * Get all values that match the given (full) field path. Subpaths can be separated by a dot '.'. If lists are included in the path, all values from every list entry is collected.
   *
   * @param object the object to get the values from
   * @param fieldName name of the field to get the values for. subfields separated by '.'
   * @param <T> data structure of the result values
   * @return a list containing all the values of the given field that are contained in the given object
   */
  @SuppressWarnings("unchecked")
  protected static <T> List<T> getValuesFromObject(Map<String, Object> object, String fieldName) {
    if (fieldName.contains(".")) {
      int idx = fieldName.indexOf(".");
      String firstField = fieldName.substring(0, idx);
      String remainingFields = fieldName.substring(idx + 1);
      Object value = object.get(firstField);
      if (value == null) {
        return new ArrayList<>();
      } else if (value instanceof List<?>) {
        return ((List<?>) value).stream()
                .map(item -> (List<T>) getValuesFromObject((Map<String, Object>) item, remainingFields))
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
      } else if (value instanceof Map<?, ?>) {
        return getValuesFromObject((Map<String, Object>) value, remainingFields);
      }
      throw new IllegalArgumentException();
    }
    Object value = object.get(fieldName);
    if (value == null) {
      return new ArrayList<>();
    }
    if (value instanceof List<?>) {
      return (List<T>) value;
    }
    return List.of((T) value);
  }

}
