package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.VocabItem;
import org.oersi.repository.VocabItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Setter
public class VocabServiceImpl implements VocabService {

  private final @NonNull VocabItemRepository vocabItemRepository;

  @Transactional
  @Override
  public List<VocabItem> updateVocab(String vocabIdentifier, List<VocabItem> items) {
    List<VocabItem> existing = vocabItemRepository.findByVocabIdentifier(vocabIdentifier);
    vocabItemRepository.deleteAll(existing);
    return vocabItemRepository.saveAll(items);
  }
}
