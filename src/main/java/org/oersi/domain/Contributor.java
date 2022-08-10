package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
public class Contributor extends BaseEntity {

  private String type;
  private String identifier;
  private String name;
  private String honorificPrefix;
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Affiliation affiliation;

}
