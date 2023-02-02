package org.oersi.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Dynamic;

@Data
@Document(indexName = "oersi_backend_config", dynamic = Dynamic.FALSE)
public class BackendConfig {

  @Id
  private String id = "oersi_backend_config";

  private String metadataIndexName = null;
  private String additionalMetadataIndexName = null;

}
