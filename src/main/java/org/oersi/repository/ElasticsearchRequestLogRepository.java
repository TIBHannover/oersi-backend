package org.oersi.repository;

import org.oersi.domain.ElasticsearchRequestLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticsearchRequestLogRepository extends ElasticsearchRepository<ElasticsearchRequestLog, String>  {
}
