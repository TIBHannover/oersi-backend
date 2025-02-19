package org.sidre.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(indexName = "search_index_backend_metadata_enrichment", dynamic = Dynamic.FALSE)
public class BackendMetadataEnrichment {

  @Id
  private String id;

  @Field(type = FieldType.Text)
  private String restrictionMetadataId;

  @Field(type= FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime dateUpdated;

  private Map<String, Boolean> onlyExtended;
  private Map<String, Object> fieldValues;

}
