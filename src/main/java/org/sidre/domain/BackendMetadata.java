package org.sidre.domain;

import lombok.Data;
import org.sidre.AutoUpdateInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Dynamic;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(indexName = "search_index_backend_metadata", dynamic = Dynamic.FALSE)
public class BackendMetadata {

  @Id
  private String id;

  private Map<String, Object> data;
  private Map<String, Object> extendedData;

  private OembedInfo oembedInfo;

  @Transient
  private AutoUpdateInfo autoUpdateInfo;

  @Field(type=FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime dateModified;

  public Object get(String fieldName) {
    return data.get(fieldName);
  }
  public static String mapToElasticsearchPath(String fieldPath) {
    return "data." + fieldPath;
  }

}
