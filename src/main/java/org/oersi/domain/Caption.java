package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class Caption {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String identifier;
  private String encodingFormat;
  private String inLanguage;
  private String type = "MediaObject";

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Caption caption = (Caption) o;
    return id != null && Objects.equals(id, caption.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
