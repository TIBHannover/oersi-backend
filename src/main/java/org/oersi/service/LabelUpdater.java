package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.*;
import org.oersi.repository.LabelDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelUpdater {

  private final @NonNull LabelDefinitionRepository labelDefinitionRepository;

  public void addMissingLabels(Metadata metadata) {
    if (metadata.getAbout() != null) {
      for (About about : metadata.getAbout()) {
        about.setPrefLabel(addMissingLabels(about.getIdentifier(), about.getPrefLabel()));
      }
    }
    if (metadata.getAudience() != null) {
      for (Audience audience : metadata.getAudience()) {
        audience.setPrefLabel(addMissingLabels(audience.getIdentifier(), audience.getPrefLabel()));
      }
    }
    if (metadata.getConditionsOfAccess() != null) {
      ConditionsOfAccess coa = metadata.getConditionsOfAccess();
      coa.setPrefLabel(addMissingLabels(coa.getIdentifier(), coa.getPrefLabel()));
    }
    if (metadata.getLearningResourceType() != null) {
      for (LearningResourceType lrt : metadata.getLearningResourceType()) {
        lrt.setPrefLabel(addMissingLabels(lrt.getIdentifier(), lrt.getPrefLabel()));
      }
    }
  }

  public LocalizedString addMissingLabels(String identifier, LocalizedString existingLabels) {
    LabelDefinition labelDefinition = getLabelDefinition(identifier);
    LocalizedString result = existingLabels;
    if (labelDefinition != null && labelDefinition.getLabel() != null && labelDefinition.getLabel().getLocalizedStrings() != null) {
      if (result == null) {
        result = new LocalizedString();
        result.setLocalizedStrings(new HashMap<>());
      } else if (result.getLocalizedStrings() == null) {
        result.setLocalizedStrings(new HashMap<>());
      }
      Map<String, String> localizedStrings = result.getLocalizedStrings();

      for (Map.Entry<String, String> definitionEntry : labelDefinition.getLabel().getLocalizedStrings().entrySet()) {
        if (localizedStrings.containsKey(definitionEntry.getKey())) {
          continue;
        }
        localizedStrings.put(definitionEntry.getKey(), definitionEntry.getValue());
      }
    }
    return result;
  }

  private LabelDefinition getLabelDefinition(String identifier) {
    Optional<LabelDefinition> labelDefinitionSearch = labelDefinitionRepository.findByIdentifier(identifier);
    return labelDefinitionSearch.orElse(null);
  }

}
