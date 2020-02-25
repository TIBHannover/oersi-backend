package eu.tib.oersi.service;

import eu.tib.oersi.domain.OerMetadata;
import eu.tib.oersi.repository.OerMetadataRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link OerMetadataService}.
 */
@Service
@Slf4j
public class OerMetadataServiceImpl implements OerMetadataService {

  @Autowired
  private OerMetadataRepository oerMeatadataRepository;

  @Transactional
  @Override
  public OerMetadata createOrUpdate(final OerMetadata metadata) {
    OerMetadata existingMetadata = findMatchingMetadata(metadata);
    if (existingMetadata != null) {
      log.debug("existing data: {}", existingMetadata);
      metadata.setId(existingMetadata.getId());
    }
    metadata.setDateModifiedInternal(LocalDateTime.now());
    return oerMeatadataRepository.save(metadata);
  }

  /**
   * Find an existing {@link OerMetadata} that matches the given {@link OerMetadata}.
   * @param metadata existing data has to match this data
   * @return existing data or null, if not existing
   */
  private OerMetadata findMatchingMetadata(final OerMetadata metadata) {
    OerMetadata existingMetadata = findById(metadata.getId());
    if (existingMetadata == null) {
      String url = metadata.getWork().getUrl();
      if (url != null) {
        List<OerMetadata> metadataMatchingUrl = oerMeatadataRepository.findByWorkUrl(url);
        if (!metadataMatchingUrl.isEmpty()) {
          existingMetadata = metadataMatchingUrl.get(0);
        }
      }
    }
    return existingMetadata;
  }

  @Transactional
  @Override
  public void delete(final OerMetadata metadata) {
    oerMeatadataRepository.delete(metadata);
  }

  @Transactional(readOnly = true)
  @Override
  public List<OerMetadata> findAll() {
    return oerMeatadataRepository.findAll();
  }

  @Transactional(readOnly = true)
  @Override
  public OerMetadata findById(final Long id) {
    if (id == null) {
      return null;
    }
    Optional<OerMetadata> optional = oerMeatadataRepository.findById(id);
    if (optional.isPresent()) {
      return optional.get();
    }
    return null;
  }

}
