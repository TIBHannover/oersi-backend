package org.sidre.repository;

import org.sidre.domain.ElasticsearchRequestLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticsearchRequestLogRepository extends ElasticsearchRepository<ElasticsearchRequestLog, String>  {
}
