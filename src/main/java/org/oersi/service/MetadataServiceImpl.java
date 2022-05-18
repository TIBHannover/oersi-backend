package org.oersi.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.*;
import org.oersi.repository.LabelDefinitionRepository;
import org.oersi.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of {@link MetadataService}.
 */
@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataServiceImpl implements MetadataService {

  private static final String LABEL_GROUP_ID_AUDIENCE = "audience";
  private static final String LABEL_GROUP_ID_CONDITIONS_OF_ACCESS = "conditionsOfAccess";
  private static final String LABEL_GROUP_ID_LRT = "lrt";
  private static final String LABEL_GROUP_ID_SUBJECT = "subject";

  private final @NonNull MetadataRepository oerMetadataRepository;
  private final @NonNull LabelDefinitionRepository labelDefinitionRepository;
  private final @NonNull LabelService labelService;
  private final @NonNull MetadataAutoUpdater metadataAutoUpdater;

  @Value("${feature.add_missing_labels}")
  private boolean featureAddMissingLabels;

  @Value("${feature.add_missing_metadata_infos}")
  private boolean featureAddMissingMetadataInfos;

  @Transactional
  @Override
  public Metadata createOrUpdate(final Metadata metadata) {
    LabelUpdater labelUpdater = new LabelUpdater(labelDefinitionRepository);
    addDefaultValues(metadata);
    ValidatorResult validatorResult = new MetadataValidator(metadata).validate();
    if (!validatorResult.isValid()) {
      log.debug("invalid data: {}, violations: {}", metadata, validatorResult.getViolations());
      throw new IllegalArgumentException(String.join(", ", validatorResult.getViolations()));
    }
    Metadata existingMetadata = findMatchingMetadata(metadata);
    if (existingMetadata != null) {
      log.debug("existing data: {}", existingMetadata);
      metadata.setId(existingMetadata.getId());
      // we need to update the existing list here, otherwise the existing list-entity remains in the
      // session without association to a parent entity and an error occurs
      // see https://gitlab.com/oersi/oersi-backend/-/issues/9
      metadata.setAbout(updateExistingList(existingMetadata.getAbout(), metadata.getAbout()));
      metadata.setAudience(updateExistingList(existingMetadata.getAudience(), metadata.getAudience()));
      metadata.setCaption(updateExistingList(existingMetadata.getCaption(), metadata.getCaption()));
      metadata.setCreator(updateExistingList(existingMetadata.getCreator(), metadata.getCreator()));
      metadata.setContributor(updateExistingList(existingMetadata.getContributor(), metadata.getContributor()));
      metadata.setLearningResourceType(updateExistingList(existingMetadata.getLearningResourceType(), metadata.getLearningResourceType()));
      metadata.setMainEntityOfPage(mergeMainEntityOfPageList(existingMetadata.getMainEntityOfPage(),
          metadata.getMainEntityOfPage()));
      metadata.setPublisher(updateExistingList(existingMetadata.getPublisher(), metadata.getPublisher()));
      metadata.setSourceOrganization(updateExistingList(existingMetadata.getSourceOrganization(),
          metadata.getSourceOrganization()));
      metadata.setEncoding(updateExistingList(existingMetadata.getEncoding(), metadata.getEncoding()));
    }
    metadata.setDateModifiedInternal(LocalDateTime.now());
    metadata.setName(cutString(metadata.getName(), Metadata.NAME_LENGTH));
    metadata.setDescription(cutString(metadata.getDescription(), Metadata.DESCRIPTION_LENGTH));
    determineProviderNames(metadata);
    if (featureAddMissingLabels) {
      labelUpdater.addMissingLabels(metadata);
    }
    if (featureAddMissingMetadataInfos) {
      metadataAutoUpdater.addMissingInfos(metadata);
    }
    storeLabels(metadata);
    return oerMetadataRepository.save(metadata);
  }

  private void addDefaultValues(final Metadata metadata) {
    if (CollectionUtils.isEmpty(metadata.getType())) {
      metadata.setType(new ArrayList<>(List.of("LearningResource")));
    }
    if (metadata.getIsAccessibleForFree() == null) {
      metadata.setIsAccessibleForFree(true);
    }
  }

  /**
   * Use the @{@link LabelService} to store all labels contained in this @{@link Metadata}.
   * @param metadata metadata
   */
  private void storeLabels(final Metadata metadata) {
    if (metadata.getAbout() != null) {
      for (About about : metadata.getAbout()) {
        storeLabels(about.getIdentifier(), about.getPrefLabel(), LABEL_GROUP_ID_SUBJECT);
      }
    }
    if (metadata.getAudience() != null) {
      for (Audience audience : metadata.getAudience()) {
        storeLabels(audience.getIdentifier(), audience.getPrefLabel(), LABEL_GROUP_ID_AUDIENCE);
      }
    }
    if (metadata.getConditionsOfAccess() != null) {
      storeLabels(metadata.getConditionsOfAccess().getIdentifier(), metadata.getConditionsOfAccess().getPrefLabel(), LABEL_GROUP_ID_CONDITIONS_OF_ACCESS);
    }
    if (metadata.getLearningResourceType() != null) {
      for (LearningResourceType lrt : metadata.getLearningResourceType()) {
        storeLabels(lrt.getIdentifier(), lrt.getPrefLabel(), LABEL_GROUP_ID_LRT);
      }
    }
  }
  private void storeLabels(final String key, final LocalizedString prefLabel, final String groupId) {
    if (key == null || prefLabel == null || prefLabel.getLocalizedStrings() == null) {
      return;
    }
    Map<String, String> localizedStrings = prefLabel.getLocalizedStrings();
    for (Map.Entry<String, String> entry : localizedStrings.entrySet()) {
      labelService.createOrUpdate(entry.getKey(), key, entry.getValue(), groupId);
    }
  }

  private String cutString(final String input, final int maxLength) {
    if (input == null) {
      return null;
    }
    return input.substring(0, Math.min(input.length(), maxLength));
  }

  /**
   * Merge existing list and new list. Entries in new list will override existing ones (based on
   * identifier).
   * 
   * @param existingList existing list
   * @param newValues new list
   * @return merged list
   */
  private List<MainEntityOfPage> mergeMainEntityOfPageList(
      final List<MainEntityOfPage> existingList, final List<MainEntityOfPage> newValues) {
    if (existingList == null) {
      return newValues;
    }
    if (newValues != null) {
      Set<String> newIds = newValues.stream()
          .map(MainEntityOfPage::getIdentifier)
          .collect(Collectors.toSet());
      List<MainEntityOfPage> overwriteEntries = existingList.stream()
          .filter(m -> newIds.contains(m.getIdentifier()))
          .collect(Collectors.toList());
      existingList.removeAll(overwriteEntries);
      existingList.addAll(newValues);
    }
    return existingList;
  }

  private <T> List<T> updateExistingList(final List<T> existingList, final List<T> newValues) {
    if (existingList == null) {
      return newValues;
    }
    existingList.clear();
    if (newValues != null) {
      existingList.addAll(newValues);
    }
    return existingList;
  }

  private String getDomainName(final String url) throws URISyntaxException {
    URI uri = new URI(url);
    String domain = uri.getHost();
    return (domain != null && domain.startsWith("www.")) ? domain.substring(4) : domain;
  }

  private void determineProviderNames(final Metadata metadata) {
    if (metadata.getMainEntityOfPage() != null) {
      metadata.getMainEntityOfPage().forEach(this::determineProviderName);
    }
  }

  private void determineProviderName(final MainEntityOfPage mainEntityOfPage) {
    Provider provider = mainEntityOfPage.getProvider();
    boolean missingProviderName = provider == null || provider.getName() == null;
    if (mainEntityOfPage.getIdentifier() != null && missingProviderName) {
      if (provider == null) {
        provider = new Provider();
        mainEntityOfPage.setProvider(provider);
      }
      String sourceUrl = mainEntityOfPage.getIdentifier();
      try {
        provider.setName(getDomainName(sourceUrl));
      } catch (URISyntaxException e) {
        log.warn("invalid uri {}", e.getMessage());
      }
    }
  }

  /**
   * Find an existing {@link Metadata} that matches the given {@link Metadata}.
   *
   * @param metadata existing data has to match this data
   * @return existing data or null, if not existing
   */
  private Metadata findMatchingMetadata(final Metadata metadata) {
    Metadata existingMetadata = findById(metadata.getId());
    if (existingMetadata == null) {
      List<Metadata> metadataMatchingUrl = oerMetadataRepository.findByIdentifier(metadata.getIdentifier());
      if (!metadataMatchingUrl.isEmpty()) {
        existingMetadata = metadataMatchingUrl.get(0);
      }
    }
    return existingMetadata;
  }

  @Transactional
  @Override
  public void delete(final Metadata metadata) {
    oerMetadataRepository.delete(metadata);
  }

  @Transactional
  @Override
  public void deleteAll() {
    log.info("delete all metadata");
    oerMetadataRepository.deleteAll();
  }

  @Transactional(readOnly = true)
  @Override
  public Metadata findById(final Long id) {
    if (id == null) {
      return null;
    }
    Optional<Metadata> optional = oerMetadataRepository.findById(id);
    return optional.orElse(null);
  }

}
