package org.oersi.repository;

import org.oersi.domain.LabelDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for @{@link LabelDefinition}.
 */
public interface LabelDefinitionRepository extends JpaRepository<LabelDefinition, Long> {

}
