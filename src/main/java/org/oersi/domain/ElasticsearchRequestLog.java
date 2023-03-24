package org.oersi.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Document(indexName = "oersi_backend_elasticsearch_request_log-#{T(java.time.LocalDate).now().toString()}")
public class ElasticsearchRequestLog {
    @Id
    private String id;
    private String method;
    private String path;
    @Field(type= FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime timestamp;
    private String body;
    private Long resultTook;
    private Integer resultHitsTotal;
}
