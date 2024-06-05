package org.sidre.repository;

import org.sidre.domain.BackendConfig;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BackendConfigRepository extends ElasticsearchRepository<BackendConfig, String>, BackendConfigUpdateRepository {
}
