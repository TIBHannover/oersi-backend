package eu.tib.oersi.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

/**
 * Representation of an author.
 */
@Data
@Entity
public class Author {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String givenName;
  @Column(nullable = false)
  private String familyName;
  private String orcid;
  private String gnd;
}
