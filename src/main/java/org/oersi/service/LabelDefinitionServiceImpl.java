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
    return labelDefinitionRepository.saveAll(labelsToUpdate);
  }

  @Transactional
  @Override
  public void delete(LabelDefinition labelDefinition) {
    labelDefinitionRepository.delete(labelDefinition);
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
}
