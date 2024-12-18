package org.sidre.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.VocabItem;
import org.sidre.repository.VocabItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelServiceImpl implements LabelService {

  private final @NonNull ConfigService configService;
  private final @NonNull VocabItemRepository vocabItemRepository;

  private Map<String, Map<String, String>> labelByLanguage = null;
  private Map<String, Map<String, Map<String, String>>> labelByLanguageAndVocab = null;

  private Map<String, Map<String, String>> initLabelByLanguageCache(Iterable<VocabItem> vocabItems) {
    Map<String, Map<String, String>> result = Collections.synchronizedMap(new HashMap<>());
    for (VocabItem item : vocabItems) {
      if (item.getPrefLabel() != null) {
        item.getPrefLabel().keySet().forEach(lng ->  {
          Map<String, String> languageLabels = result.computeIfAbsent(lng, k -> Collections.synchronizedMap(new HashMap<>()));
          languageLabels.put(item.getItemKey(), item.getPrefLabel().get(lng));
        });
      }
    }
    labelByLanguage = result;
    return result;
  }



  private Map<String, Map<String, Map<String, String>>> initLabelByLanguageAndVocabCache(Iterable<VocabItem> vocabItems) {
    Map<String, Map<String, Map<String, String>>> result = Collections.synchronizedMap(new HashMap<>());
    for (VocabItem item : vocabItems) {
      if (item.getPrefLabel() != null) {
        item.getPrefLabel().keySet().forEach(lng -> {
          Map<String, Map<String, String>> languageLabels = result.computeIfAbsent(lng, k -> Collections.synchronizedMap(new HashMap<>()));
          Map<String, String> vocabLabels = languageLabels.computeIfAbsent(item.getVocabIdentifier(), k -> Collections.synchronizedMap(new HashMap<>()));
          vocabLabels.put(item.getItemKey(), item.getPrefLabel().get(lng));
        });
      }
    }
    labelByLanguageAndVocab = result;
    return result;
  }

  private Map<String, Map<String, String>> getLabelByLanguageCache() {
    Map<String, Map<String, String>> result = labelByLanguage;
    if (result == null) {
      log.debug("Init label cache (byLanguage)");
      Iterable<VocabItem> vocabItems = vocabItemRepository.findAll();
      result = initLabelByLanguageCache(vocabItems);
    }
    return result;
  }

  private Map<String, Map<String, Map<String, String>>> getLabelByLanguageAndVocabCache() {
    Map<String, Map<String, Map<String, String>>> result = labelByLanguageAndVocab;
    if (result == null) {
      log.debug("Init label cache (byLanguageAndField)");
      Iterable<VocabItem> vocabItems = vocabItemRepository.findAll();
      result = initLabelByLanguageAndVocabCache(vocabItems);
    }
    return result;
  }

  @Transactional(readOnly = true)
  @Override
  public Map<String, String> findByLanguage(String languageCode) {
    return new HashMap<>(getLabelByLanguageCache().computeIfAbsent(languageCode, k -> new HashMap<>()));
  }

  @Transactional(readOnly = true)
  @Override
  public Map<String, String> findByLanguageAndField(String languageCode, String field) {
    final String vocabId = getVocabIdForField(field);
    if (vocabId == null) {
      return new HashMap<>();
    }
    return new HashMap<>(getLabelByLanguageAndVocabCache()
      .computeIfAbsent(languageCode, k -> new HashMap<>())
      .computeIfAbsent(vocabId, k -> new HashMap<>()));
  }

  private String getVocabIdForField(String field) {
    BackendConfig config = configService.getMetadataConfig();
    if (field != null && config != null && config.getFieldProperties() != null) {
      BackendConfig.FieldProperties fieldProperties = config.getFieldProperties().stream().filter(p -> field.equals(p.getFieldName())).findFirst().orElse(null);
      if (fieldProperties != null) {
        return fieldProperties.getVocabIdentifier();
      }
    }
    return null;
  }

  @Override
  public void clearCache() {
    labelByLanguage = null;
    labelByLanguageAndVocab = null;
  }

}
