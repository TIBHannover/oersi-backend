package eu.tib.oersi.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

/**
 * DidacticsDto metadata.
 */
@Data
@Entity
public class Didactics {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String educationalUse;
  @Column(nullable = false)
  private String audience;
  @Column(nullable = false)
  private String interactivityType;
  @Column(nullable = false)
  private String timeRequired;
}
