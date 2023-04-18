package org.oersi.service;

import java.util.Map;

public interface LabelService {

  Map<String, String> findByLanguage(String languageCode);
  /** @deprecated replaced by findByLanguageAndField */
  @Deprecated(forRemoval = true)
  Map<String, String> findByLanguageAndGroup(String languageCode, String groupId);
  Map<String, String> findByLanguageAndField(String languageCode, String field);
  void clearCache();

}
