package eu.tib.oersi.service;

import eu.tib.oersi.domain.OerMetadata;

/**
 * Manage {@link OerMetadata}s.
 */
public interface OerMetadataService {

  /**
   * Create or update the given {@link OerMetadata}.
   *
   * @param metadata metadata
   * @return updated metadata
   */
  OerMetadata createOrUpdate(OerMetadata metadata);

  /**
   * Delete the given {@link OerMetadata}.
   *
   * @param metadata metadata
   */
  void delete(OerMetadata metadata);

  /**
   * Retrieve {@link OerMetadata} for the given id.
   *
   * @param id id
   * @return data
   */
  OerMetadata findById(Long id);

}
