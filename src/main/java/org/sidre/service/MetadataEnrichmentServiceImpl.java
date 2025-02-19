package org.sidre.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.BackendMetadataEnrichment;
import org.sidre.repository.MetadataEnrichmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataEnrichmentServiceImpl implements MetadataEnrichmentService {

  private final @NonNull MetadataEnrichmentRepository metadataEnrichmentRepository;

  @Transactional(readOnly = true)
  @Override
  public List<BackendMetadataEnrichment> findMetadataEnrichmentsByRestrictionMetadataId(String metadataId) {
    return metadataEnrichmentRepository.findByRestrictionMetadataId(metadataId);
  }

  @Transactional(readOnly = true)
  @Override
  public List<BackendMetadataEnrichment> findMetadataEnrichments(int page, int size) {
    return metadataEnrichmentRepository.findAll(PageRequest.of(page, size)).toList();
  }

  @Transactional
  @Override
  public void createOrUpdate(List<BackendMetadataEnrichment> enrichments) {
    enrichments.forEach(enrichment -> {
      enrichment.setDateUpdated(LocalDateTime.now());
      if (enrichment.getRestrictionMetadataId() != null) {
        List<BackendMetadataEnrichment> existingEnrichments = metadataEnrichmentRepository.findByRestrictionMetadataId(enrichment.getRestrictionMetadataId());
        if (!existingEnrichments.isEmpty()) {
          enrichment.setId(existingEnrichments.get(0).getId());  // there should be only one existing enrichment, so we can safely use the first one
        }
      }
    });
    metadataEnrichmentRepository.saveAll(enrichments);
    log.debug("Saved {} metadata enrichments", enrichments.size());
  }

  @Transactional
  @Override
  public void deleteByIds(List<String> enrichmentIds) {
    metadataEnrichmentRepository.deleteAllById(enrichmentIds);
  }

  @Override
  public void addMetadataEnrichments(BackendMetadata metadata) {
    List<BackendMetadataEnrichment> existingEnrichments = metadataEnrichmentRepository.findByRestrictionMetadataId(metadata.getId());
    if (!existingEnrichments.isEmpty()) {
      addMetadataEnrichments(metadata, existingEnrichments.get(0));
    }
  }

  @Override
  public void addMetadataEnrichments(BackendMetadata metadata, BackendMetadataEnrichment enrichment) {
    // currently only flat fields are supported, this means fields that are not nested and accessible in the root metadata
    Map<String, Boolean> onlyExtended = enrichment.getOnlyExtended() == null ? new HashMap<>() : enrichment.getOnlyExtended();
    for (String field : enrichment.getFieldValues().keySet()) {
      var value = enrichment.getFieldValues().get(field);
      if (Boolean.FALSE.equals(onlyExtended.getOrDefault(field, false))) {
        metadata.getData().put(field, value);
      }
      if (metadata.getExtendedData() != null) {
        metadata.getExtendedData().put(field, value);
      }
    }
  }
}
