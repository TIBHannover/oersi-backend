package eu.tib.oersi.domain;

import java.time.LocalDate;
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
import org.hibernate.validator.constraints.URL;

/**
 * Contains the metadata for an oer document
 */
@Data
@Entity
public class Metadata {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @URL
  @Column(nullable = false)
  private String identifier;

  @Column(nullable = false)
  private String name;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<Creator> creator;

  @Column(length = 5000)
  private String description;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<About> about;

  @URL
  private String license;

  @URL
  private String image;

  private LocalDate dateCreated;
  private LocalDate datePublished;
  private String inLanguage;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private LearningResourceType learningResourceType;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Audience audience;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<MainEntityOfPage> mainEntityOfPage;

  @Column(nullable = false)
  private LocalDateTime dateModifiedInternal;

}
