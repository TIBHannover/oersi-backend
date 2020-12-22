package org.oersi.service;

import java.util.Locale;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.oersi.domain.About;
import org.oersi.domain.LocalizedString;
import org.oersi.domain.Metadata;

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
    if (metadata.getAbout() != null) {
      for (About about : metadata.getAbout()) {
        validatePrefLabel(about.getPrefLabel());
      }
    }
    if (metadata.getAudience() != null) {
      validatePrefLabel(metadata.getAudience().getPrefLabel());
    }
    if (metadata.getLearningResourceType() != null) {
      validatePrefLabel(metadata.getLearningResourceType().getPrefLabel());
    }
    return result;
  }

  private void validatePrefLabel(final LocalizedString prefLabel) {
    if (prefLabel != null && prefLabel.getLocalizedStrings() != null) {
      prefLabel.getLocalizedStrings().keySet().stream().filter(s -> !ISO_LANGUAGES.contains(s))
          .forEach(s -> result.addViolation("Illegal language code '" + s + "'"));
    }
  }

}
