package org.oersi.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "oersi_backend_label")
public class Label {

  @Id
  private String id;
  private String groupId;
  private String languageCode;
  private String labelKey;
  private String labelValue;

}
