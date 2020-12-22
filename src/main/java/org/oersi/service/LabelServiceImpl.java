package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.Label;
import org.oersi.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelServiceImpl implements LabelService {

  private final @NonNull LabelRepository labelRepository;

  @Override
  public Label createOrUpdate(String languageCode, String labelKey, String labelValue, String groupId) {
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
    label.setGroupId(groupId);
    return labelRepository.save(label);
  }

  @Override
  public Map<String, String> findByLanguage(String languageCode) {
    Map<String, String> result = new HashMap<>();
    for (Label label : labelRepository.findByLanguageCode(languageCode)) {
      result.put(label.getLabelKey(), label.getLabelValue());
    }
    return result;
  }

  @Override
  public Map<String, String> findByLanguageAndGroup(String languageCode, String groupId) {
    Map<String, String> result = new HashMap<>();
    for (Label label : labelRepository.findByLanguageCodeAndGroupId(languageCode, groupId)) {
      result.put(label.getLabelKey(), label.getLabelValue());
    }
    return result;
  }

}
