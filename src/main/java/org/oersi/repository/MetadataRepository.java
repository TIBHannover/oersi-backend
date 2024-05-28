package org.oersi.repository;

import org.oersi.domain.BackendMetadata;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MetadataRepository extends ElasticsearchRepository<BackendMetadata, String> {
}
