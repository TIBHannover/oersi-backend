package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Objects;

/**
 * Definition of all available labels in the backend core data.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class LabelDefinition {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, unique = true, length = 191)
  private String identifier;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private LocalizedString label;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LabelDefinition that = (LabelDefinition) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
