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
public class Provider extends BaseEntity {

  private String type;
  private String identifier;
  private String name;
}
