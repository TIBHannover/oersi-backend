package eu.tib.oersi.service;

import eu.tib.oersi.domain.MainEntityOfPage;
import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.domain.Provider;
import eu.tib.oersi.repository.MetadataRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link MetadataService}.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataServiceImpl implements MetadataService {

  private final @NonNull MetadataRepository oerMeatadataRepository;

  @Transactional
  @Override
  public Metadata createOrUpdate(final Metadata metadata) {
    Metadata existingMetadata = findMatchingMetadata(metadata);
    if (existingMetadata != null) {
      log.debug("existing data: {}", existingMetadata);
      metadata.setId(existingMetadata.getId());
      // we need to update the existing list here, otherwise the existing list-entity remains in the
      // session without association to a parent entity and an error occurs
      // see https://gitlab.com/oersi/oersi-backend/-/issues/9
      metadata.setAbout(updateExistingList(existingMetadata.getAbout(), metadata.getAbout()));
      metadata.setCreator(updateExistingList(existingMetadata.getCreator(), metadata.getCreator()));
      metadata.setMainEntityOfPage(updateExistingList(existingMetadata.getMainEntityOfPage(),
          metadata.getMainEntityOfPage()));
      metadata.setSourceOrganization(updateExistingList(existingMetadata.getSourceOrganization(),
          metadata.getSourceOrganization()));
    }
    metadata.setDateModifiedInternal(LocalDateTime.now());
    determineProviderNames(metadata);
    return oerMeatadataRepository.save(metadata);
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
      String url = metadata.getIdentifier();
      if (url != null) {
        List<Metadata> metadataMatchingUrl = oerMeatadataRepository.findByIdentifier(url);
        if (!metadataMatchingUrl.isEmpty()) {
          existingMetadata = metadataMatchingUrl.get(0);
        }
      }
    }
    return existingMetadata;
  }

  @Transactional
  @Override
  public void delete(final Metadata metadata) {
    oerMeatadataRepository.delete(metadata);
  }

  @Transactional(readOnly = true)
  @Override
  public Metadata findById(final Long id) {
    if (id == null) {
      return null;
    }
    Optional<Metadata> optional = oerMeatadataRepository.findById(id);
    if (optional.isPresent()) {
      return optional.get();
    }
    return null;
  }

}
