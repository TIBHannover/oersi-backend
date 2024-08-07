package org.sidre.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Dynamic;

import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "search_index_backend_config", dynamic = Dynamic.FALSE)
public class BackendConfig {

  @Data
  public static class FieldProperties {
    private String fieldName;
    private String vocabIdentifier;
    private String vocabItemIdentifierField;
    private String vocabItemLabelField;
    private boolean addMissingVocabLabels;
    private boolean addMissingVocabParents;
  }
  @Data
  public static class DefaultFieldProperties {
    private String vocabItemIdentifierField;
    private String vocabItemLabelField;
  }

  @Id
  private String id = "search_index_backend_config";

  private String metadataIndexName = null;
  private String extendedMetadataIndexName = null;
  private Map<String, Object> customConfig;
  private List<FieldProperties> fieldProperties;
  private DefaultFieldProperties defaultFieldProperties;

}
