package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Entity;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
public class Media extends BaseEntity {

  private String bitrate;

  @URL
  private String contentUrl;

  private String contentSize;

  @URL
  private String embedUrl;

  private String encodingFormat;

  private String sha256;

  private String type = "MediaObject";

}
