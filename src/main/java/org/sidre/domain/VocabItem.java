package org.sidre.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;

@Data
@Document(indexName = "search_index_backend_vocab_item")
public class VocabItem {

  @Id
  private String id;
  private String vocabIdentifier;
  private String itemKey;
  private String parentKey;
  private Map<String, String> prefLabel;

}
