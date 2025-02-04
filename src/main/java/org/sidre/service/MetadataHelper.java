package org.sidre.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataHelper {
  private MetadataHelper(){}

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static <T> List<T> parseList(Map<String, Object> properties, String fieldName, TypeReference<T> listEntryTypeRef) {
    var list = properties.get(fieldName);
    if (!(list instanceof List<?>)) {
      return null;
    }
    return ((List<?>) list).stream().map(e -> objectMapper.convertValue(e, listEntryTypeRef)).collect(Collectors.toCollection(ArrayList::new));
  }

  public static <T> T parse(Map<String, Object> properties, String fieldName, TypeReference<T> typeRef) {
    Object value = properties.get(fieldName);
    if (value == null) {
      return null;
    }
    return objectMapper.convertValue(value, typeRef);
  }

  public static Map<String, Object> format(Object o) {
    return objectMapper.convertValue(o, new TypeReference<>() {});
  }

  public interface ObjectModifier {
    void modify(Map<String, Object> object);
  }

  /**
   * Modify the object at the given field name with the given modifier. Consider sub-objects - also modify all objects in the object tree.
   */
  public static void modifyObjectTree(Map<String, Object> properties, String fieldName, ObjectModifier modifier) {
    List<String> fields = Arrays.asList(fieldName.split("\\."));
    String firstField = fields.get(0);
    boolean hasSubFields = fields.size() > 1;
    String remainingFields = hasSubFields ? String.join(".", fields.subList(1, fields.size())) : null;
    Object o = properties.get(firstField);
    if (o != null) {
      if (o instanceof List<?>) {
        if (hasSubFields) {
          MetadataHelper.modifyObjectList(properties, firstField, current -> modifyObjectTree(current, remainingFields, modifier));
        } else {
          MetadataHelper.modifyObjectList(properties, fieldName, modifier);
        }
      } else {
        if (hasSubFields) {
          MetadataHelper.modifyObject(properties, firstField, current -> modifyObjectTree(current, remainingFields, modifier));
        } else {
          MetadataHelper.modifyObject(properties, fieldName, modifier);
        }
      }
    }
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
