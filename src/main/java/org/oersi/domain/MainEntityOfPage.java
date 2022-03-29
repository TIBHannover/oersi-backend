package org.oersi.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class MainEntityOfPage {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String identifier;
  private String type;
  private String dateCreated;
  private String dateModified;
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Provider provider;

}
