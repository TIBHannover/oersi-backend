package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Definition of all available labels in the backend core data.
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
public class LabelDefinition extends BaseEntity {

  @Column(nullable = false, unique = true, length = 191)
  private String identifier;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private LocalizedString label;

}
