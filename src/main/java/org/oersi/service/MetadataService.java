package org.oersi.service;

import org.oersi.domain.Metadata;

/**
 * Manage {@link Metadata}s.
 */
public interface MetadataService {

  /**
   * Create or update the given {@link Metadata}.
   *
   * @param metadata metadata
   * @return updated metadata
   */
  Metadata createOrUpdate(Metadata metadata);

  /**
   * Delete the given {@link Metadata}.
   *
   * @param metadata metadata
   */
  void delete(Metadata metadata);

  /**
   * Delete all existing {@link Metadata}.
   */
  void deleteAll();

  /**
   * Delete existing {@link Metadata} that match the given provider name.
   *
   * @param providerName provider name
   */
  void deleteByProviderName(String providerName);

  /**
   * Retrieve {@link Metadata} for the given id.
   *
   * @param id id
   * @return data
   */
  Metadata findById(Long id);

}
