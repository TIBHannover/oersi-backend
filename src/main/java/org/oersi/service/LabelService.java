package org.oersi.service;

import org.oersi.domain.Label;

import java.util.Map;

public interface LabelService {

  Label createOrUpdate(String languageCode, String labelKey, String labelValue, String groupId);
  Map<String, String> findByLanguage(String languageCode);
  Map<String, String> findByLanguageAndGroup(String languageCode, String groupId);

}
