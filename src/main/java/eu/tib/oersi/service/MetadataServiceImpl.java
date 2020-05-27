package eu.tib.oersi.service;

import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.repository.MetadataRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  private void determineSource(final Metadata metadata) {
    if (metadata.getMainEntityOfPage() != null
        && metadata.getMainEntityOfPage().getIdentifier() != null) {
      String sourceUrl = metadata.getMainEntityOfPage().getIdentifier();
      final String pattern = "^.*://([A-Za-z0-9\\-\\.]+).*$";
      Matcher m = Pattern.compile(pattern).matcher(sourceUrl);
      if (m.find()) {
        metadata.getMainEntityOfPage().setSource(m.group(1));
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
