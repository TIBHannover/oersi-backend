package org.sidre.service;

import java.util.Map;

public interface LabelService {

  Map<String, String> findByLanguage(String languageCode);
  Map<String, String> findByLanguageAndField(String languageCode, String field);
  void clearCache();

}
