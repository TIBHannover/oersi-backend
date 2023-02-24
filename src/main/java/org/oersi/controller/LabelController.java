package org.oersi.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oersi.api.LabelControllerApi;
import org.oersi.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelController implements LabelControllerApi {

  private final @NonNull LabelService labelService;

  @Deprecated
  @Override
  public ResponseEntity<Object> retrieveByLanguageAndGroup(@PathVariable String language, @RequestParam(required = false) String vocab) {
    Map<String, String> result;
    if (StringUtils.isBlank(vocab)) {
      result = labelService.findByLanguage(language);
    } else {
      result = labelService.findByLanguageAndGroup(language, vocab);
    }
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<Object> retrieveByLanguageAndField(@PathVariable String language, @RequestParam(required = false) String field) {
    Map<String, String> result;
    if (StringUtils.isBlank(field)) {
      result = labelService.findByLanguage(language);
    } else {
      result = labelService.findByLanguageAndField(language, field);
    }
    return ResponseEntity.ok(result);
  }

}
