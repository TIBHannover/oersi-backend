package org.oersi.service;

import java.util.Locale;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.oersi.domain.*;

/**
 * Validator for {@link Metadata}.
 */
@RequiredArgsConstructor
public class MetadataValidator {

  private static final Set<String> ISO_LANGUAGES = Set.of(Locale.getISOLanguages());

  private final @NonNull Metadata metadata;

  private ValidatorResult result;

  /**
   * Validate this {@link Metadata}.
   *
   * @return result of the validation
   */
  public ValidatorResult validate() {
    result = new ValidatorResult();
    validateMandatoryFields();
    if (metadata.getAbout() != null) {
      for (About about : metadata.getAbout()) {
        validatePrefLabel(about.getPrefLabel());
      }
    }
    if (metadata.getAudience() != null) {
      for (Audience audience : metadata.getAudience()) {
        validatePrefLabel(audience.getPrefLabel());
      }
    }
    if (metadata.getLearningResourceType() != null) {
      for (LearningResourceType lrt : metadata.getLearningResourceType()) {
        validatePrefLabel(lrt.getPrefLabel());
      }
    }
    return result;
  }

  private void validateMandatoryFields() {
    if (StringUtils.isEmpty(metadata.getIdentifier())) {
      result.addViolation("Empty mandatory field 'identifier'");
    }
    if (StringUtils.isEmpty(metadata.getName())) {
      result.addViolation("Empty mandatory field 'name'");
    }
  }

  private void validatePrefLabel(final LocalizedString prefLabel) {
    if (prefLabel != null && prefLabel.getLocalizedStrings() != null) {
      prefLabel.getLocalizedStrings().keySet().stream().filter(s -> !ISO_LANGUAGES.contains(s))
          .forEach(s -> result.addViolation("Illegal language code '" + s + "'"));
    }
  }

}
