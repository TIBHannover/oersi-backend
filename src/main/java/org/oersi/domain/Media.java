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

  @URL
  private String embedUrl;

  private String encodingFormat;

  private String type = "MediaObject";

}
