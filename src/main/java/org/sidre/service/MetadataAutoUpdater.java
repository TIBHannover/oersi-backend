package org.sidre.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sidre.AutoUpdateInfo;
import org.sidre.AutoUpdateProperties;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OembedInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class that sets missing infos at {@link org.sidre.domain.BackendMetadata}
 */
@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Setter
public class MetadataAutoUpdater {

  private final @NonNull AutoUpdateProperties autoUpdateProperties;
  private final @NonNull MetadataFieldService metadataFieldService;
  private final @NonNull ConfigService configService;
  private final @NonNull VocabService vocabService;

  public void initAutoUpdateInfo(BackendMetadata data) {
    AutoUpdateInfo info = new AutoUpdateInfo();
    for (AutoUpdateProperties.Entry definition : autoUpdateProperties.getDefinitions()) {
      var pattern = Pattern.compile(definition.getRegex());
      var id = metadataFieldService.getIdentifier(data.getData());
      var matcher = pattern.matcher(id);
      if (matcher.matches()) {
        info.setEmbedUrl(getEmbedUrl(id, definition, matcher));
        info.setProviderName(definition.getProviderName());
        info.setProviderUrl(definition.getProviderUrl());
        break;
      }
    }
    data.setAutoUpdateInfo(info);
  }
  public void addMissingInfos(BackendMetadata data) {
    addMissingParentItemsForHierarchicalVocab(data);
    addMissingLabels(data);
  }

  public OembedInfo initOembedInfo(BackendMetadata data) {
    AutoUpdateInfo info = data.getAutoUpdateInfo();
    OembedInfo oembedInfo = new OembedInfo();
    oembedInfo.setProviderName(info.getProviderName());
    oembedInfo.setProviderUrl(info.getProviderUrl());
    oembedInfo.setVideoEmbedUrl(info.getEmbedUrl());
    return oembedInfo;
  }

  private String getEmbedUrl(String id, AutoUpdateProperties.Entry definition, Matcher matcher) {
    var embedUrl = definition.getEmbedUrl();
    if (embedUrl == null) {
      return null; // no embed url in this definition -> skip
    }
    log.debug("{} matches {}", definition.getRegex(), id);
    for (int i = 1; i <= matcher.groupCount(); i++) {
      embedUrl = embedUrl.replace("###" + i + "###", matcher.group(i));
    }
    log.debug("embed url {}", embedUrl);
    return embedUrl;
  }

  private interface FieldPropertiesSelector {
    boolean select(BackendConfig.FieldProperties fieldProperties);
  }

  private List<BackendConfig.FieldProperties> getFieldProperties(FieldPropertiesSelector selector) {
    BackendConfig config = configService.getMetadataConfig();
    if (config != null && config.getFieldProperties() != null) {
      return config.getFieldProperties().stream().filter(selector::select).toList();
    }
    return new ArrayList<>();
  }

  private void addMissingParentItemsForHierarchicalVocab(BackendMetadata data) {
    for (BackendConfig.FieldProperties fieldProp : getFieldProperties(BackendConfig.FieldProperties::isAddMissingVocabParents)) {
      String fieldName = fieldProp.getFieldName();
      String vocabItemIdentifierField = fieldProp.getVocabItemIdentifierField();
      Map<String, String> parentMap = vocabService.getParentMap(fieldProp.getVocabIdentifier());
      boolean isObject = !StringUtils.isEmpty(vocabItemIdentifierField);
      if (isObject) {
        List<Map<String, Object>> fieldContent = MetadataHelper.parseList(data.getData(), fieldName, new TypeReference<>() {});
        if (fieldContent != null) {
          Set<String> ids = fieldContent.stream().map(e -> (String) e.get(vocabItemIdentifierField)).collect(Collectors.toSet());
          Set<String> idsToAdd = getParentIdsToAdd(ids, parentMap);
          for (String id: idsToAdd) {
            fieldContent.add(Map.of(vocabItemIdentifierField, id));
          }
          data.getData().put(fieldName, fieldContent);
        }
      } else {
        List<String> fieldContent = MetadataHelper.parseList(data.getData(), fieldName, new TypeReference<>() {});
        if (fieldContent != null) {
          Set<String> ids = new HashSet<>(fieldContent);
          Set<String> idsToAdd = getParentIdsToAdd(ids, parentMap);
          fieldContent.addAll(idsToAdd);
          data.getData().put(fieldName, fieldContent);
        }
      }
    }
  }

  private Set<String> getParentIdsToAdd(Set<String> ids, Map<String, String> parentMap) {
    Set<String> idsToAdd = new HashSet<>();
    for (String id : ids) {
      String parentId = parentMap.get(id);
      if (parentId != null && !ids.contains(parentId)) {
        idsToAdd.add(parentId);
      }
    }
    if (!idsToAdd.isEmpty()) {
      Set<String> allIds = new HashSet<>();
      allIds.addAll(ids);
      allIds.addAll(idsToAdd);
      idsToAdd.addAll(getParentIdsToAdd(allIds, parentMap));
    }
    return idsToAdd;
  }

  /**
   * Add default localized labels that are not defined at the given metadata.
   * @param metadata set label at this data
   */
  public void addMissingLabels(BackendMetadata metadata) {
    getFieldProperties(BackendConfig.FieldProperties::isAddMissingVocabLabels).forEach(field -> addMissingLabels(metadata, field));
  }

  private void addMissingLabels(BackendMetadata metadata, BackendConfig.FieldProperties fieldProperties) {
    String fieldName = fieldProperties.getFieldName();
    Map<String, Object> data = metadata.getData();
    if (data.get(fieldName) instanceof List) {
      List<Map<String, Object>> labelledConceptList = MetadataHelper.parseList(data, fieldName, new TypeReference<>() {});
      if (labelledConceptList != null) {
        labelledConceptList.forEach(c -> addMissingLabels(c, fieldProperties));
        data.put(fieldName, labelledConceptList);
      }
    } else {
      Map<String, Object> labelledConcept = MetadataHelper.parse(data, fieldName, new TypeReference<>() {});
      if (labelledConcept != null) {
        addMissingLabels(labelledConcept, fieldProperties);
        data.put(fieldName, labelledConcept);
      }
    }
  }

  public void addMissingLabels(Map<String, Object> labelledConcept, BackendConfig.FieldProperties fieldProperties) {
    final String labelField = fieldProperties.getVocabItemLabelField();
    Map<String, String> existingLabels = MetadataHelper.parse(labelledConcept, labelField, new TypeReference<>() {});
    Map<String, String> defaultLocalizedLabel = getDefaultLocalizedLabel((String) labelledConcept.get(fieldProperties.getVocabItemIdentifierField()));
    Map<String, String> prefLabel = existingLabels;
    if (defaultLocalizedLabel != null ) {
      if (prefLabel == null) {
        prefLabel = new HashMap<>();
      }
      for (Map.Entry<String, String> defaultLabelEntry : defaultLocalizedLabel.entrySet()) {
        if (prefLabel.containsKey(defaultLabelEntry.getKey())) {
          continue;
        }
        prefLabel.put(defaultLabelEntry.getKey(), defaultLabelEntry.getValue());
      }
    }
    if (prefLabel != null) {
      labelledConcept.put(labelField, prefLabel);
    }
  }

  private Map<String, String> getDefaultLocalizedLabel(String identifier) {
    return vocabService.findLocalizedLabelByIdentifier(identifier);
  }

}
