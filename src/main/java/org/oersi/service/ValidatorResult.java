package org.oersi.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a validation.
 */
public class ValidatorResult {

  private final List<String> violationMessages = new ArrayList<>();

  /**
   * Add a violation to this result.
   *
   * @param violationMessage
   */
  public void addViolation(final String violationMessage) {
    violationMessages.add(violationMessage);
  }

  /**
   * Check if the validation identified any violation.
   *
   * @return true, if the validated data is valid
   */
  public boolean isValid() {
    return violationMessages.isEmpty();
  }

  /**
   * Get a list of all validations.
   * 
   * @return validations
   */
  public List<String> getViolations() {
    return violationMessages;
  }

}
