package org.sidre.repository;

import org.sidre.domain.BackendMetadata;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MetadataRepository extends ElasticsearchRepository<BackendMetadata, String> {
}
