package org.oersi.repository;

import org.oersi.domain.Label;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

/**
 * Repository for @{@link Label}.
 */
public interface LabelRepository extends ElasticsearchRepository<Label, String> {

  Optional<Label> findByLanguageCodeAndLabelKey(String languageCode, String labelKey);

}
