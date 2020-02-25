package eu.tib.oersi.domain;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Represents the description of the oer material.
 */
@Data
@Entity
public class EducationalResource {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(length = 5000)
  private String description;

  @Column(nullable = false)
  private String subject;

  @ElementCollection
  @CollectionTable(name = "educational_resource_keywords")
  private List<String> keywords;

  @Column(nullable = false)
  private String license;

  @URL
  @Column(nullable = false)
  private String url;

  @Column(nullable = false)
  private String inLanguage;

  @Column(nullable = false)
  private String learningResourceType;

  private String version;

  private LocalDateTime dateCreated;
  private LocalDateTime dateLastUpdated;
  private LocalDateTime datePublished;

  private String identifier;

  @URL
  private String thumbnailUrl;

}
