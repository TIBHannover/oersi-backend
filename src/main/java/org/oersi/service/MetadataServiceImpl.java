package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.About;
import org.oersi.domain.Audience;
import org.oersi.domain.LearningResourceType;
import org.oersi.domain.LocalizedString;
import org.oersi.domain.MainEntityOfPage;
import org.oersi.domain.Metadata;
import org.oersi.domain.Provider;
import org.oersi.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
  private final @NonNull LabelService labelService;
  private final @NonNull MetadataAutoUpdater metadataAutoUpdater;

  @Value("${feature.add_missing_labels}")
  private boolean featureAddMissingLabels;

  @Value("${feature.add_missing_metadata_infos}")
  private boolean featureAddMissingMetadataInfos;

  @Transactional
  @Override
  public MetadataUpdateResult createOrUpdate(final Metadata metadata) {
    return createOrUpdate(List.of(metadata)).get(0);
  }

  @Transactional
  @Override
  public List<MetadataUpdateResult> createOrUpdate(final List<Metadata> records) {
    List<MetadataUpdateResult> results = new ArrayList<>();
    for (Metadata metadata: records) {
      MetadataUpdateResult result = new MetadataUpdateResult(metadata);
      addDefaultValues(metadata);
      ValidatorResult validatorResult = new MetadataValidator(metadata).validate();
      if (!validatorResult.isValid()) {
        log.debug("invalid data: {}, violations: {}", metadata, validatorResult.getViolations());
        result.setSuccess(false);
        result.addMessages(validatorResult.getViolations());
        results.add(result);
        continue;
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
        metadata.setAssesses(updateExistingList(existingMetadata.getAssesses(), metadata.getAssesses()));
        metadata.setCaption(updateExistingList(existingMetadata.getCaption(), metadata.getCaption()));
        metadata.setCompetencyRequired(updateExistingList(existingMetadata.getCompetencyRequired(), metadata.getCompetencyRequired()));
        metadata.setCreator(updateExistingList(existingMetadata.getCreator(), metadata.getCreator()));
        metadata.setContributor(updateExistingList(existingMetadata.getContributor(), metadata.getContributor()));
        metadata.setEducationalLevel(updateExistingList(existingMetadata.getEducationalLevel(), metadata.getEducationalLevel()));
        metadata.setLearningResourceType(updateExistingList(existingMetadata.getLearningResourceType(), metadata.getLearningResourceType()));
        metadata.setMainEntityOfPage(mergeMainEntityOfPageList(existingMetadata.getMainEntityOfPage(),
          metadata.getMainEntityOfPage()));
        metadata.setPublisher(updateExistingList(existingMetadata.getPublisher(), metadata.getPublisher()));
        metadata.setSourceOrganization(updateExistingList(existingMetadata.getSourceOrganization(),
          metadata.getSourceOrganization()));
        metadata.setEncoding(updateExistingList(existingMetadata.getEncoding(), metadata.getEncoding()));
        metadata.setTeaches(updateExistingList(existingMetadata.getTeaches(), metadata.getTeaches()));
      }
      metadata.setDateModifiedInternal(LocalDateTime.now());
      metadata.setRecordStatusInternal(Metadata.RecordStatus.ACTIVE);
      metadata.setName(cutString(metadata.getName(), Metadata.NAME_LENGTH));
      metadata.setDescription(cutString(metadata.getDescription(), Metadata.DESCRIPTION_LENGTH));
      determineProviderNames(metadata);
      if (featureAddMissingMetadataInfos) {
        metadataAutoUpdater.addMissingInfos(metadata);
      }
      if (featureAddMissingLabels) {
        metadataAutoUpdater.addMissingLabels(metadata);
      }
      storeLabels(metadata);
      results.add(result);
    }
    List<Metadata> dataToUpdate = results.stream().filter(MetadataUpdateResult::getSuccess).map(MetadataUpdateResult::getMetadata).collect(Collectors.toList());
    if (!dataToUpdate.isEmpty()) {
      oerMetadataRepository.saveAll(dataToUpdate);
    }
    return results;
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
    log.debug("delete metadata with identifier {}", metadata.getIdentifier());
    delete(List.of(metadata));
  }

  private void delete(final List<Metadata> metadata) {
    oerMetadataRepository.deleteAll(metadata);
  }

  @Transactional
  @Override
  public void deleteAll() {
    log.info("delete all metadata");
    oerMetadataRepository.deleteAll();
  }

  @Transactional
  @Override
  public void deleteMainEntityOfPageByProviderName(String providerName) {
    log.info("delete mainEntityOfPage in metadata for provider {}", providerName);
    final int pageSize = 100;
    Long lastId = 0L;
    List<Metadata> metadata = oerMetadataRepository.findByMainEntityOfPageProviderNameAndIdGreaterThanOrderByIdAsc(providerName, lastId, PageRequest.ofSize(pageSize));
    while (!metadata.isEmpty()) {
      metadata.forEach(data -> {
        data.getMainEntityOfPage().removeIf(m -> m.getProvider() != null && providerName.equals(m.getProvider().getName()));
        data.setDateModifiedInternal(LocalDateTime.now());
      });
      oerMetadataRepository.saveAll(metadata);
      delete(metadata.stream().filter(m -> m.getMainEntityOfPage().isEmpty()).collect(Collectors.toList()));

      lastId = metadata.get(metadata.size() - 1).getId();
      metadata = oerMetadataRepository.findByMainEntityOfPageProviderNameAndIdGreaterThanOrderByIdAsc(providerName, lastId, PageRequest.ofSize(pageSize));
    }
  }

  @Transactional
  @Override
  public boolean deleteMainEntityOfPageByIdentifier(final String mainEntityOfPageId) {
    List<Metadata> metadata = findByMainEntityOfPageId(mainEntityOfPageId);
    if (metadata.isEmpty()) {
      return false;
    }
    metadata.forEach(data -> {
      data.getMainEntityOfPage().removeIf(m -> m.getIdentifier().equals(mainEntityOfPageId));
      data.setDateModifiedInternal(LocalDateTime.now());
    });
    oerMetadataRepository.saveAll(metadata);
    metadata.stream().filter(m -> m.getMainEntityOfPage().isEmpty()).forEach(this::delete);
    return true;
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

  @Transactional(readOnly = true)
  @Override
  public List<Metadata> findByMainEntityOfPageId(final String mainEntityOfPageIdentifier) {
    return oerMetadataRepository.findByMainEntityOfPageIdentifier(mainEntityOfPageIdentifier);
  }
}
