package org.oersi.domain;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
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

}
