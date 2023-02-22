package org.oersi.repository;

import org.oersi.domain.VocabItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface VocabItemRepository extends ElasticsearchRepository<VocabItem, String> {

  List<VocabItem> findByVocabIdentifier(String vocabIdentifier);

}
