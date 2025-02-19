package org.sidre.repository;

import org.sidre.domain.BackendMetadataEnrichment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface MetadataEnrichmentRepository extends ElasticsearchRepository<BackendMetadataEnrichment, String> {

  List<BackendMetadataEnrichment> findByRestrictionMetadataId(String metadataId);
}
