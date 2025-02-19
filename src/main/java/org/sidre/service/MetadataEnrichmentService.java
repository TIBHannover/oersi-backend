package org.sidre.service;

import org.sidre.domain.BackendMetadata;
import org.sidre.domain.BackendMetadataEnrichment;

import java.util.List;

public interface MetadataEnrichmentService {

  /**
   * Find all metadata enrichments that are restricted to a metadata ID.
   * @param metadataId restriction to this metadata ID
   * @return list of matching metadata enrichments
   */
  List<BackendMetadataEnrichment> findMetadataEnrichmentsByRestrictionMetadataId(String metadataId);
  /**
   * Find all metadata enrichments (paginated).
   * @param page page number, starting with 0
   * @param size page size
   * @return list of metadata enrichments
   */
  List<BackendMetadataEnrichment> findMetadataEnrichments(int page, int size);
  /**
   * Create or update the given enrichments. Enrichments that are restricted to a metadata ID will overwrite an existing enrichment for this metadata, other enrichments will be created new.
   * @param enrichments enrichments to be created or updated
   */
  void createOrUpdate(List<BackendMetadataEnrichment> enrichments);
  /**
   * Delete the enrichments with the given ids.
   * @param enrichmentIds ids of the enrichments to be deleted
   */
  void deleteByIds(List<String> enrichmentIds);
  /**
   * Find the enrichment related to the metadata and process the enrichment, if exists. The fields at the metadata are updated according to the enrichment.
   * @param metadata metadata to be enriched
   */
  void addMetadataEnrichments(BackendMetadata metadata);
  /**
   * Process the given metadata enrichment. The fields at the metadata are updated according to the enrichment.
   * @param metadata metadata to be enriched
   * @param enrichment enrichment to be applied
   */
  void addMetadataEnrichments(BackendMetadata metadata, BackendMetadataEnrichment enrichment);

}
