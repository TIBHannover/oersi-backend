package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity(name = "pref_label")
public class LocalizedString {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ElementCollection
  @CollectionTable(name = "localized_string",
      joinColumns = {@JoinColumn(name = "pref_label_id", referencedColumnName = "id")})
  @MapKeyColumn(name = "language_code", length = 5)
  @Column(name = "label")
  private Map<String, String> localizedStrings;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LocalizedString that = (LocalizedString) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
