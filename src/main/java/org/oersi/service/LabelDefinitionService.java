package org.oersi.service;

import org.oersi.domain.LabelDefinition;

import java.util.List;

/**
 * Manage {@link LabelDefinition}s.
 */
public interface LabelDefinitionService {

  /**
   * Create or update the given {@link LabelDefinition}s.
   *
   * @param labelDefinitions labelDefinitions
   * @return updated labelDefinitions
   */
  List<LabelDefinition> createOrUpdate(List<LabelDefinition> labelDefinitions);

  /**
   * Delete the given {@link LabelDefinition}.
   *
   * @param labelDefinition labelDefinition
   */
  void delete(LabelDefinition labelDefinition);

  /**
   * Retrieve {@link LabelDefinition} for the given id.
   *
   * @param id id
   * @return data
   */
  LabelDefinition findById(Long id);
  
}