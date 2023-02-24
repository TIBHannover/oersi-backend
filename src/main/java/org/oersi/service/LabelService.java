package org.oersi.service;

import org.oersi.domain.Label;

import java.util.Map;

public interface LabelService {

  Label createOrUpdate(String languageCode, String labelKey, String labelValue, String fieldName);
  Map<String, String> findByLanguage(String languageCode);
  @Deprecated
  Map<String, String> findByLanguageAndGroup(String languageCode, String groupId);
  Map<String, String> findByLanguageAndField(String languageCode, String field);
  void clearCache();
  void init();

}
