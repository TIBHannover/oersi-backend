package eu.tib.oersi.dto;

import lombok.Data;

/**
 * Data transfer object for person.
 */
@Data
public class AuthorDto {
  private Long id;
  private String givenName;
  private String familyName;
  private String orcid;
  private String gnd;
}
