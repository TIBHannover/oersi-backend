package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oersi.domain.Label;
import org.oersi.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelServiceImpl implements LabelService {

  @Deprecated
  private static final String LABEL_GROUP_ID_AUDIENCE = "audience";
  @Deprecated
  private static final String LABEL_GROUP_ID_CONDITIONS_OF_ACCESS = "conditionsOfAccess";
  @Deprecated
  private static final String LABEL_GROUP_ID_LRT = "lrt";
  @Deprecated
  private static final String LABEL_GROUP_ID_SUBJECT = "subject";
  @Deprecated
  private static final Map<String, String> GROUP_TO_FIELD_MAPPING = Map.of(
    LABEL_GROUP_ID_AUDIENCE, LABEL_GROUP_ID_AUDIENCE,
    LABEL_GROUP_ID_CONDITIONS_OF_ACCESS, LABEL_GROUP_ID_CONDITIONS_OF_ACCESS,
    LABEL_GROUP_ID_LRT, "learningResourceType",
    LABEL_GROUP_ID_SUBJECT, "about"
  );

  private final @NonNull LabelRepository labelRepository;

  private Map<String, Map<String, String>> labelByLanguage = null;
  private Map<String, Map<String, Map<String, String>>> labelByLanguageAndField = null;

  @Transactional
  @Override
  public Label createOrUpdate(String languageCode, String labelKey, String labelValue, String field) {
    String currentValue = findByLanguageAndField(languageCode, field).get(labelKey);
    if (StringUtils.equals(currentValue, labelValue)) {
      return null;
    }
    Optional<Label> existing = labelRepository.findByLanguageCodeAndLabelKey(languageCode, labelKey);
    Label label;
    if (existing.isPresent()) {
      label = existing.get();
    } else {
      label = new Label();
      label.setLanguageCode(languageCode);
      label.setLabelKey(labelKey);
    }
    label.setLabelValue(labelValue);
    label.setField(field);
    log.debug("Update label {}", label);
    Label savedLabel;
    synchronized (labelRepository) {
      savedLabel = labelRepository.save(label);
      clearCache();
    }
    return savedLabel;
  }

  private Map<String, Map<String, String>> initLabelByLanguageCache(Iterable<Label> labels) {
    Map<String, Map<String, String>> result = Collections.synchronizedMap(new HashMap<>());
    for (Label label : labels) {
      Map<String, String> languageLabels = result.computeIfAbsent(label.getLanguageCode(), k -> Collections.synchronizedMap(new HashMap<>()));
      languageLabels.put(label.getLabelKey(), label.getLabelValue());
    }
    labelByLanguage = result;
    return result;
  }

  private Map<String, Map<String, Map<String, String>>> initLabelByLanguageAndFieldCache(Iterable<Label> labels) {
    Map<String, Map<String, Map<String, String>>> result = Collections.synchronizedMap(new HashMap<>());
    for (Label label : labels) {
      Map<String, Map<String, String>> languageLabels = result.computeIfAbsent(label.getLanguageCode(), k -> Collections.synchronizedMap(new HashMap<>()));
      Map<String, String> fieldLabels = languageLabels.computeIfAbsent(label.getField(), k -> Collections.synchronizedMap(new HashMap<>()));
      fieldLabels.put(label.getLabelKey(), label.getLabelValue());
    }
    labelByLanguageAndField = result;
    return result;
  }

  private Map<String, Map<String, String>> getLabelByLanguageCache() {
    Map<String, Map<String, String>> result = labelByLanguage;
    if (result == null) {
      log.debug("Init label cache (byLanguage)");
      synchronized (labelRepository) {
        Iterable<Label> labels = labelRepository.findAll();
        result = initLabelByLanguageCache(labels);
      }
    }
    return result;
  }

  private Map<String, Map<String, Map<String, String>>> getLabelByLanguageAndFieldCache() {
    Map<String, Map<String, Map<String, String>>> result = labelByLanguageAndField;
    if (result == null) {
      log.debug("Init label cache (byLanguageAndField)");
      synchronized (labelRepository) {
        Iterable<Label> labels = labelRepository.findAll();
        result = initLabelByLanguageAndFieldCache(labels);
      }
    }
    return result;
  }

  @Transactional(readOnly = true)
  @Override
  public Map<String, String> findByLanguage(String languageCode) {
    return new HashMap<>(getLabelByLanguageCache().computeIfAbsent(languageCode, k -> new HashMap<>()));
  }

  @Deprecated
  @Transactional(readOnly = true)
  @Override
  public Map<String, String> findByLanguageAndGroup(String languageCode, String groupId) {
    String field = GROUP_TO_FIELD_MAPPING.get(groupId);
    return findByLanguageAndField(languageCode, field);
  }

  @Transactional(readOnly = true)
  @Override
  public Map<String, String> findByLanguageAndField(String languageCode, String field) {
    return new HashMap<>(getLabelByLanguageAndFieldCache()
      .computeIfAbsent(languageCode, k -> new HashMap<>())
      .computeIfAbsent(field, k -> new HashMap<>()));
  }

  @Override
  public void clearCache() {
    labelByLanguage = null;
    labelByLanguageAndField = null;
  }

  @Transactional
  @Override
  public void init() {
    synchronized (labelRepository) {
      Iterable<Label> labels = labelRepository.findAll();
      StreamSupport.stream(labels.spliterator(), false).filter(l -> l.getField() == null).forEach(l -> {
        l.setField(GROUP_TO_FIELD_MAPPING.get(l.getGroupId()));
        labelRepository.save(l);
      });
    }
    getLabelByLanguageCache();
  }

}
