package eu.tib.oersi.domain;

import java.time.LocalDate;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class MainEntityOfPage {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String identifier;
  private String type;
  private LocalDate dateCreated;
  private LocalDate dateModified;
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Provider provider;

}
