package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.VocabItem;
import org.oersi.repository.VocabItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Setter
public class VocabServiceImpl implements VocabService {

  private final @NonNull LabelService labelService;
  private final @NonNull VocabItemRepository vocabItemRepository;

  private Map<String, Map<String, String>> localizedLabelByIdentifier;

  @Transactional
  @Override
  public Iterable<VocabItem> updateVocab(String vocabIdentifier, List<VocabItem> items) {
    List<VocabItem> existing = vocabItemRepository.findByVocabIdentifier(vocabIdentifier);
    synchronized (vocabItemRepository) {
      vocabItemRepository.deleteAll(existing);
      var result = vocabItemRepository.saveAll(items);
      clearCache();
      return result;
    }
  }

  @Transactional
  @Override
  public Map<String, String> getParentMap(String vocabIdentifier) {
    List<VocabItem> items = vocabItemRepository.findByVocabIdentifier(vocabIdentifier);
    Map<String, String> result = new HashMap<>();
    for (VocabItem item: items) {
      result.put(item.getItemKey(), item.getParentKey());
    }
    return result;
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public Iterable<VocabItem> findAll() {
    return vocabItemRepository.findAll();
  }

  @Override
  public Map<String, String> findLocalizedLabelByIdentifier(String identifier) {
    return getLocalizedLabelByIdentifierCache().get(identifier);
  }
  private Map<String, Map<String, String>> getLocalizedLabelByIdentifierCache() {
    Map<String, Map<String, String>> result = localizedLabelByIdentifier;
    if (result == null) {
      log.debug("Init localized label cache (byIdentifier)");
      synchronized (vocabItemRepository) {
        Iterable<VocabItem> labelDefinitions = findAll();
        result = initLocalizedLabelByIdentifierCache(labelDefinitions);
      }
    }
    return result;
  }
  private Map<String, Map<String, String>> initLocalizedLabelByIdentifierCache(Iterable<VocabItem> labelDefinitions) {
    Map<String, Map<String, String>> result = Collections.synchronizedMap(new HashMap<>());
    for (VocabItem labelDefinition : labelDefinitions) {
      Map<String, String> languageLabels = result.computeIfAbsent(labelDefinition.getItemKey(), k -> Collections.synchronizedMap(new HashMap<>()));
      if (labelDefinition.getPrefLabel() != null) {
        languageLabels.putAll(labelDefinition.getPrefLabel());
      }
    }
    localizedLabelByIdentifier = result;
    return result;
  }

  public void clearCache() {
    localizedLabelByIdentifier = null;
    labelService.clearCache();
  }

}
