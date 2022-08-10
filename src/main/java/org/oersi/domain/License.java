package org.oersi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
public class License extends BaseEntity {

  @Column(nullable = false)
  @URL
  private String identifier;

}
