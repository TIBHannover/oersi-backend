package org.oersi.domain;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Data
@Entity
public class ConditionsOfAccess {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String identifier;
  private String type;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private LocalizedString prefLabel;

}
