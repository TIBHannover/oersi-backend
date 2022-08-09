package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class ConditionsOfAccess {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String identifier;
  private String type;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private LocalizedString prefLabel;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ConditionsOfAccess that = (ConditionsOfAccess) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
