package org.oersi.repository;

import org.oersi.domain.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for @{@link Label}.
 */
public interface LabelRepository extends JpaRepository<Label, Long> {

  Optional<Label> findByLanguageCodeAndLabelKey(String languageCode, String labelKey);
  List<Label> findByLanguageCode(String languageCode);
  List<Label> findByLanguageCodeAndGroupId(String languageCode, String groupId);

}
