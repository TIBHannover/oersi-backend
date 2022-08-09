package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

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
public class Media {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String bitrate;

  @URL
  private String contentUrl;

  private String contentSize;

  @URL
  private String embedUrl;

  private String encodingFormat;

  private String sha256;

  private String type = "MediaObject";

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Media media = (Media) o;
    return id != null && Objects.equals(id, media.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
