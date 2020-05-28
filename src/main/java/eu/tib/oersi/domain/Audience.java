package eu.tib.oersi.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Audience {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String identifier;

}
