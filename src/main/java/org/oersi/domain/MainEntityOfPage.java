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
public class MainEntityOfPage extends BaseEntity {

  private String identifier;
  private String type;
  private String dateCreated;
  private String dateModified;
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Provider provider;
}
