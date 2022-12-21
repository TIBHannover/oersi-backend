package org.oersi.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.oersi.domain.*;

/**
 * Validator for {@link Metadata}.
 */
@RequiredArgsConstructor
public class MetadataValidator {

  private static final int DEFAULT_MAX_STRING_LENGTH = 200;
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
    validateUrls();
    validateFieldLength();
    validatePrefLabels();
    return result;
  }

  private void validateUrls() {
    UrlValidator urlValidator = new UrlValidator();
    validateUrl(urlValidator, metadata.getIdentifier());
    validateUrl(urlValidator, metadata.getImage());
    validateUrl(urlValidator, metadata.getContextUri());
    if (metadata.getLicense() != null) {
      validateUrl(urlValidator, metadata.getLicense().getIdentifier());
    }
    if (metadata.getEncoding() != null) {
      metadata.getEncoding().forEach(e -> validateUrl(urlValidator, e.getEmbedUrl()));
      metadata.getEncoding().forEach(e -> validateUrl(urlValidator, e.getContentUrl()));
    }
    if (metadata.getTrailer() != null) {
      validateUrl(urlValidator, metadata.getTrailer().getEmbedUrl());
      validateUrl(urlValidator, metadata.getTrailer().getContentUrl());
    }
  }
  private void validateUrl(UrlValidator urlValidator, String value) {
    if (value != null && !urlValidator.isValid(value)) {
      result.addViolation("Invalid URL '" + value + "'");
    }
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

  private <T extends LabelledConcept> void validatePrefLabels(final List<T> labelledConcepts) {
    if (labelledConcepts != null) {
      labelledConcepts.forEach(s -> validatePrefLabel(s.getPrefLabel()));
    }
  }

  private <T extends LabelledConcept> void validatePrefLabel(final T labelledConcept) {
    if (labelledConcept != null) {
      validatePrefLabel(labelledConcept.getPrefLabel());
    }
  }

  private void validatePrefLabels() {
    validatePrefLabels(metadata.getAbout());
    validatePrefLabels(metadata.getAudience());
    validatePrefLabel(metadata.getConditionsOfAccess());
    validatePrefLabels(metadata.getCompetencyRequired());
    validatePrefLabels(metadata.getAssesses());
    validatePrefLabels(metadata.getEducationalLevel());
    validatePrefLabels(metadata.getTeaches());
    validatePrefLabels(metadata.getLearningResourceType());
  }

  private void validateLength(String s, int maxLength) {
    if (s.length() > maxLength) {
      result.addViolation("Value max length exceeded: " + s);
    }
  }
  private void validateFieldLength() {
    if (metadata.getKeywords() != null) {
      metadata.getKeywords().forEach(k -> validateLength(k, DEFAULT_MAX_STRING_LENGTH));
    }
  }

}
