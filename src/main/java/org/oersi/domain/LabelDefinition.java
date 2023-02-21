package org.oersi.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;

/**
 * Definition of all available labels in the backend core data.
 */
@Data
@Document(indexName = "oersi_backend_label_definition")
public class LabelDefinition {

  @Id
  private String id;
  private String identifier;
  private Map<String, String> label;

}
