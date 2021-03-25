package org.oersi.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * Definition of all available labels in the backend core data.
 */
@Data
@Entity
public class LabelDefinition {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, unique = true, length = 191)
  private String identifier;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private LocalizedString label;

}
