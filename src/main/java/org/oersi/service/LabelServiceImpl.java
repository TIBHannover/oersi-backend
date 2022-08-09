package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oersi.domain.Label;
import org.oersi.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelServiceImpl implements LabelService {

  private final @NonNull LabelRepository labelRepository;

  private Map<String, Map<String, String>> labelByLanguage = null;
  private Map<String, Map<String, Map<String, String>>> labelByLanguageAndGroup = null;

  @Override
  public Label createOrUpdate(String languageCode, String labelKey, String labelValue, String groupId) {
    Optional<Label> existing = labelRepository.findByLanguageCodeAndLabelKey(languageCode, labelKey);
    Label label;
    if (existing.isPresent()) {
      label = existing.get();
      if (StringUtils.equals(label.getLabelValue(), labelValue) && StringUtils.equals(label.getGroupId(), groupId)) {
        log.debug("Skip label update, because there is no change {}", label);
        return label;
      }
    } else {
      label = new Label();
      label.setLanguageCode(languageCode);
      label.setLabelKey(labelKey);
    }
    label.setLabelValue(labelValue);
    label.setGroupId(groupId);
    log.debug("Update label {}", label);
    Label savedLabel;
    synchronized (labelRepository) {
      savedLabel = labelRepository.save(label);
      clearCache();
    }
    return savedLabel;
  }

  private Map<String, Map<String, String>> initLabelByLanguageCache(List<Label> labels) {
    Map<String, Map<String, String>> result = Collections.synchronizedMap(new HashMap<>());
    for (Label label : labels) {
      Map<String, String> languageLabels = result.computeIfAbsent(label.getLanguageCode(), k -> Collections.synchronizedMap(new HashMap<>()));
      languageLabels.put(label.getLabelKey(), label.getLabelValue());
    }
    labelByLanguage = result;
    return result;
  }

  private Map<String, Map<String, Map<String, String>>> initLabelByLanguageAndGroupCache(List<Label> labels) {
    Map<String, Map<String, Map<String, String>>> result = Collections.synchronizedMap(new HashMap<>());
    for (Label label : labels) {
      Map<String, Map<String, String>> languageLabels = result.computeIfAbsent(label.getLanguageCode(), k -> Collections.synchronizedMap(new HashMap<>()));
      Map<String, String> groupLabels = languageLabels.computeIfAbsent(label.getGroupId(), k -> Collections.synchronizedMap(new HashMap<>()));
      groupLabels.put(label.getLabelKey(), label.getLabelValue());
    }
    labelByLanguageAndGroup = result;
    return result;
  }

  private Map<String, Map<String, String>> getLabelByLanguageCache() {
    Map<String, Map<String, String>> result = labelByLanguage;
    if (result == null) {
      log.debug("Init label cache (byLanguage)");
      synchronized (labelRepository) {
        List<Label> labels = labelRepository.findAll();
        result = initLabelByLanguageCache(labels);
      }
    }
    return result;
  }

  private Map<String, Map<String, Map<String, String>>> getLabelByLanguageAndGroupCache() {
    Map<String, Map<String, Map<String, String>>> result = labelByLanguageAndGroup;
    if (result == null) {
      log.debug("Init label cache (byLanguageAndGroup)");
      synchronized (labelRepository) {
        List<Label> labels = labelRepository.findAll();
        result = initLabelByLanguageAndGroupCache(labels);
      }
    }
    return result;
  }

  @Override
  public Map<String, String> findByLanguage(String languageCode) {
    return new HashMap<>(getLabelByLanguageCache().computeIfAbsent(languageCode, k -> new HashMap<>()));
  }

  @Override
  public Map<String, String> findByLanguageAndGroup(String languageCode, String groupId) {
    return new HashMap<>(getLabelByLanguageAndGroupCache()
      .computeIfAbsent(languageCode, k -> new HashMap<>())
      .computeIfAbsent(groupId, k -> new HashMap<>()));
  }

  @Override
  public void clearCache() {
    labelByLanguage = null;
    labelByLanguageAndGroup = null;
  }

}
