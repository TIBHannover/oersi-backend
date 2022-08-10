package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
public class Caption extends BaseEntity {

  private String identifier;
  private String encodingFormat;
  private String inLanguage;
  private String type = "MediaObject";

}
