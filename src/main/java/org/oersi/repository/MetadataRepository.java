package org.oersi.repository;

import org.oersi.domain.BackendMetadata;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface MetadataRepository extends ElasticsearchRepository<BackendMetadata, String> {

  @Query("{\"bool\": {\"must\": [{\"match\": {\"data.mainEntityOfPage.id\": \"?0\"}}]}}")
  List<BackendMetadata> findByMainEntityOfPageId(String mainEntityOfPageId);

}
