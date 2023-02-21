package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.LabelDefinition;
import org.oersi.repository.LabelDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelDefinitionServiceImpl implements LabelDefinitionService {

  private final @NonNull LabelDefinitionRepository labelDefinitionRepository;

  private Map<String, Map<String, String>> localizedLabelByIdentifier;

  @Transactional
  @Override
  public Iterable<LabelDefinition> createOrUpdate(List<LabelDefinition> labelDefinitions) {
    log.info("Update {} label definitions", labelDefinitions.size());
    Iterable<LabelDefinition> existingLabels = labelDefinitionRepository.findAll();
    Map<String, LabelDefinition> existingLabelsById = new HashMap<>();
    for (LabelDefinition existingLabel : existingLabels) {
      existingLabelsById.put(existingLabel.getIdentifier(), existingLabel);
    }
    List<LabelDefinition> labelsToUpdate = new ArrayList<>();
    for (LabelDefinition labelDefinition : labelDefinitions) {
      LabelDefinition existingLabel = existingLabelsById.get(labelDefinition.getIdentifier());
      if (existingLabel != null) {
        existingLabel.setLocalizedStrings(labelDefinition.getLocalizedStrings());
        labelsToUpdate.add(existingLabel);
      } else {
        labelsToUpdate.add(labelDefinition);
      }
    }
    synchronized (labelDefinitionRepository) {
      clearCache();
      return labelDefinitionRepository.saveAll(labelsToUpdate);
    }
  }

  @Transactional
  @Override
  public void delete(LabelDefinition labelDefinition) {
    synchronized (labelDefinitionRepository) {
      labelDefinitionRepository.delete(labelDefinition);
      clearCache();
    }
  }

  @Transactional(readOnly = true)
  @Override
  public LabelDefinition findById(String id) {
    if (id == null) {
      return null;
    }
    Optional<LabelDefinition> optional = labelDefinitionRepository.findById(id);
    return optional.orElse(null);
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public Iterable<LabelDefinition> findAll() {
    return labelDefinitionRepository.findAll();
  }

  @Override
  public Map<String, String> findLocalizedLabelByIdentifier(String identifier) {
    return getLocalizedLabelByIdentifierCache().get(identifier);
  }
  private Map<String, Map<String, String>> getLocalizedLabelByIdentifierCache() {
    Map<String, Map<String, String>> result = localizedLabelByIdentifier;
    if (result == null) {
      log.debug("Init localized label cache (byIdentifier)");
      synchronized (labelDefinitionRepository) {
        Iterable<LabelDefinition> labelDefinitions = findAll();
        result = initLocalizedLabelByIdentifierCache(labelDefinitions);
      }
    }
    return result;
  }
  private Map<String, Map<String, String>> initLocalizedLabelByIdentifierCache(Iterable<LabelDefinition> labelDefinitions) {
    Map<String, Map<String, String>> result = Collections.synchronizedMap(new HashMap<>());
    for (LabelDefinition labelDefinition : labelDefinitions) {
      Map<String, String> languageLabels = result.computeIfAbsent(labelDefinition.getIdentifier(), k -> Collections.synchronizedMap(new HashMap<>()));
      if (labelDefinition.getLocalizedStrings() != null) {
        languageLabels.putAll(labelDefinition.getLocalizedStrings());
      }
    }
    localizedLabelByIdentifier = result;
    return result;
  }

  public void clearCache() {
    localizedLabelByIdentifier = null;
  }
}
