package org.oersi.repository;

import org.oersi.domain.LabelDefinition;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

/**
 * Repository for @{@link LabelDefinition}.
 */
public interface LabelDefinitionRepository extends ElasticsearchRepository<LabelDefinition, String> {

  Optional<LabelDefinition> findByIdentifier(String identifier);

}
