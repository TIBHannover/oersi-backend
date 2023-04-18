package org.oersi.service;

import org.oersi.domain.VocabItem;

import java.util.List;
import java.util.Map;

public interface VocabService {

  Iterable<VocabItem> updateVocab(String vocabIdentifier, List<VocabItem> items);

  /**
   * @param vocabIdentifier identifier of the vocabulary
   * @return a map itemKey -> parentKey that represents the hierarchical structure between the items
   */
  Map<String, String> getParentMap(String vocabIdentifier);

  /**
   * Retrieve the localized labels for the given identifier.
   * @param identifier identifier to retrieve labels for
   * @return labels
   */
  Map<String, String> findLocalizedLabelByIdentifier(String identifier);

}
