package eu.tib.oersi.domain;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.Data;

/**
 * Contains the metadata for an oer document
 */
@Data
@Entity(name = OerMetadata.ENTITY_NAME)
public class OerMetadata {

  public static final String ENTITY_NAME = "oer_metadata";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "oer_metadata_id", nullable = false)
  private List<Author> authors;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
  private Work work;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Didactics didactics;

  @Column(nullable = false)
  private String source;

  @Column(nullable = false)
  private LocalDateTime dateModifiedInternal;

}
