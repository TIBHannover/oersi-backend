package org.oersi.repository;

import org.oersi.domain.LabelDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for @{@link LabelDefinition}.
 */
public interface LabelDefinitionRepository extends JpaRepository<LabelDefinition, Long> {

  Optional<LabelDefinition> findByIdentifier(String identifier);

}
