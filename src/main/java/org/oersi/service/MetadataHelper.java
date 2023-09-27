package org.oersi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oersi.domain.BackendMetadata;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataHelper {
  private MetadataHelper(){}

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static BackendMetadata toMetadata(Map<String, Object> properties) {
    BackendMetadata result = new BackendMetadata();
    result.setData(properties);
    String id = (String) properties.get("id");
    var base64Id = Base64.getUrlEncoder().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    result.setId(base64Id);
    return result;
  }

  public static <T> List<T> parseList(Map<String, Object> properties, String fieldName, TypeReference<T> listEntryTypeRef) {
    var list = properties.get(fieldName);
    if (!(list instanceof List<?>)) {
      return null;
    }
    return ((List<?>) list).stream().map(e -> objectMapper.convertValue(e, listEntryTypeRef)).collect(Collectors.toList());
  }

  public static <T> T parse(Map<String, Object> properties, String fieldName, TypeReference<T> typeRef) {
    Object value = properties.get(fieldName);
    if (value == null) {
      return null;
    }
    return objectMapper.convertValue(value, typeRef);
  }

  public interface ObjectModifier {
    void modify(Map<String, Object> object);
  }
  public static void modifyObject(Map<String, Object> properties, String fieldName, ObjectModifier modifier) {
    Map<String, Object> object = parse(properties, fieldName, new TypeReference<>() {});
    if (object != null) {
      modifier.modify(object);
      properties.put(fieldName, object);
    }
  }
  public static void modifyObjectList(Map<String, Object> properties, String fieldName, ObjectModifier modifier) {
    List<Map<String, Object>> objects = parse(properties, fieldName, new TypeReference<>() {});
    if (objects != null) {
      objects.forEach(modifier::modify);
      properties.put(fieldName, objects);
    }
  }

}
