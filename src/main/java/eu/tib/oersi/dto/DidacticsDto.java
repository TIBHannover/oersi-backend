package eu.tib.oersi.dto;

import lombok.Data;

/**
 * DidacticsDto metadata.
 */
@Data
public class DidacticsDto {
  private Long id;
  private String educationalUse;
  private String audience;
  private String interactivityType;
  private String timeRequired;
}
