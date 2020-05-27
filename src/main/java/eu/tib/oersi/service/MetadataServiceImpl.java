package eu.tib.oersi.service;

import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.repository.MetadataRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link MetadataService}.
 */
@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

  @Autowired
  private MetadataRepository oerMeatadataRepository;

  @Transactional
  @Override
  public Metadata createOrUpdate(final Metadata metadata) {
    Metadata existingMetadata = findMatchingMetadata(metadata);
    if (existingMetadata != null) {
      log.debug("existing data: {}", existingMetadata);
      metadata.setId(existingMetadata.getId());
    }
    metadata.setDateModifiedInternal(LocalDateTime.now());
    determineSource(metadata);
    return oerMeatadataRepository.save(metadata);
  }

  private String getDomainName(final String url) throws URISyntaxException {
    URI uri = new URI(url);
    String domain = uri.getHost();
    return (domain != null && domain.startsWith("www.")) ? domain.substring(4) : domain;
  }

  private void determineSource(final Metadata metadata) {
    if (metadata.getMainEntityOfPage() != null
        && metadata.getMainEntityOfPage().getIdentifier() != null) {
      String sourceUrl = metadata.getMainEntityOfPage().getIdentifier();
      try {
        metadata.getMainEntityOfPage().setSource(getDomainName(sourceUrl));
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
