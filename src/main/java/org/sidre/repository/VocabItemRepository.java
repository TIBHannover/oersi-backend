package org.sidre.repository;

import org.sidre.domain.VocabItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface VocabItemRepository extends ElasticsearchRepository<VocabItem, String> {

  List<VocabItem> findByVocabIdentifier(String vocabIdentifier);

}
