package org.oersi.repository;

import org.oersi.domain.BackendConfig;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BackendConfigRepository extends ElasticsearchRepository<BackendConfig, String>, UpdateDocumentRepository<BackendConfig> {
}
