package org.oersi.repository;

import org.oersi.domain.VocabItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VocabItemRepository extends JpaRepository<VocabItem, Long> {

  List<VocabItem> findByVocabIdentifier(String vocabIdentifier);

}
