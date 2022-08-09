package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(
  uniqueConstraints = @UniqueConstraint(columnNames = {"language_code", "label_key"})
)
public class Label {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String groupId;
  @Column(name = "language_code", nullable = false, length = 3)
  private String languageCode;
  @Column(name = "label_key", nullable = false, length = 188)
  private String labelKey;
  private String labelValue;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Label label = (Label) o;
    return id != null && Objects.equals(id, label.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
