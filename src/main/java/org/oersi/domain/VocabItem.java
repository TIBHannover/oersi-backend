package org.oersi.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "oersi_backend_vocab_item")
public class VocabItem {

  @Id
  private String id;
  private String vocabIdentifier;
  private String itemKey;
  private String parentKey;

}
