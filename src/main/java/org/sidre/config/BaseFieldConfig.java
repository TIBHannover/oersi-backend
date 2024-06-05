package org.sidre.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "base-field-config")
@Data
public class BaseFieldConfig {
  private String resourceIdentifier;
  private MetadataSource metadataSource; // identifies the sources of the metadata
  @Data
  public static class MetadataSource {
    private String field; // the field name of the field where an item/object describing the metadata source is stored.
    private String objectIdentifier; // the identifier of the object item (isObject=true). base is the object item field. Only required for isObject=true, otherwise the raw value is used as identifier.
    private Boolean useMultipleItems;  // lombok generates "getIsObject" for "Boolean" and "isObject" for "boolean". But Spring Config Properties cannot handle a "isObject" getter.
    public boolean useMultipleItems() { return Boolean.TRUE.equals(useMultipleItems); }
    private Boolean isObject;  // lombok generates "getIsObject" for "Boolean" and "isObject" for "boolean". But Spring Config Properties cannot handle a "isObject" getter.
    public boolean isObject() { return Boolean.TRUE.equals(isObject); }
    private List<Query> queries;
    @Data
    public static class Query {
      private String name;
      private String field;
    }
  }
}
