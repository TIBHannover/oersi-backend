package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.LabelDefinition;
import org.oersi.repository.LabelDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelDefinitionServiceImpl implements LabelDefinitionService {

  private final @NonNull LabelDefinitionRepository labelDefinitionRepository;

  @Transactional
  @Override
  public List<LabelDefinition> createOrUpdate(List<LabelDefinition> labelDefinitions) {
    log.info("Update {} label definitions", labelDefinitions.size());
    List<LabelDefinition> existingLabels = labelDefinitionRepository.findAll();
    Map<String, LabelDefinition> existingLabelsById = new HashMap<>();
    for (LabelDefinition existingLabel : existingLabels) {
      existingLabelsById.put(existingLabel.getIdentifier(), existingLabel);
    }
    List<LabelDefinition> labelsToUpdate = new ArrayList<>();
    for (LabelDefinition labelDefinition : labelDefinitions) {
      LabelDefinition existingLabel = existingLabelsById.get(labelDefinition.getIdentifier());
      if (existingLabel != null) {
        existingLabel.setLabel(labelDefinition.getLabel());
        labelsToUpdate.add(existingLabel);
      } else {
        labelsToUpdate.add(labelDefinition);
      }
    }
    List<LabelDefinition> result;
    synchronized (labelDefinitionRepository) {
      result = labelDefinitionRepository.saveAll(labelsToUpdate);
      clearCache();
      initLabelByLanguageCache(result);
    }
    return result;
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
  public LabelDefinition findById(Long id) {
    if (id == null) {
      return null;
    }
    Optional<LabelDefinition> optional = labelDefinitionRepository.findById(id);
    return optional.orElse(null);
  }

  @Transactional(readOnly = true)
  @Override
  public Map<String, String> getLocalizedLabelsByIdentifier(String identifier) {
    return getLocalizedLabelByIdentifierCache().get(identifier);
  }

  private Map<String, Map<String, String>> localizedLabelByIdentifier = null;
  private Map<String, Map<String, String>> initLabelByLanguageCache(List<LabelDefinition> labelDefinitions) {
    Map<String, Map<String, String>> result = Collections.synchronizedMap(new HashMap<>());
    for (LabelDefinition labelDefinition : labelDefinitions) {
      Map<String, String> localizedLabels = result.computeIfAbsent(labelDefinition.getIdentifier(), k -> Collections.synchronizedMap(new HashMap<>()));
      if (labelDefinition.getLabel() != null && labelDefinition.getLabel().getLocalizedStrings() != null) {
        localizedLabels.putAll(labelDefinition.getLabel().getLocalizedStrings());
      }
    }
    localizedLabelByIdentifier = result;
    return result;
  }
  private Map<String, Map<String, String>> getLocalizedLabelByIdentifierCache() {
    Map<String, Map<String, String>> result = localizedLabelByIdentifier;
    if (result == null) {
      log.debug("Init labelDefinition cache (byIdentifier)");
      synchronized (labelDefinitionRepository) {
        List<LabelDefinition> labelDefinitions = labelDefinitionRepository.findAll();
        result = initLabelByLanguageCache(labelDefinitions);
      }
    }
    return result;
  }

  public void clearCache() {
    localizedLabelByIdentifier = null;
  }

}
