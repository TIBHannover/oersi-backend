package org.oersi.domain;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Contains the metadata for an oer document
 */
@Data
@Entity
@Table(indexes = @Index(name = "identifier", columnList = "identifier"))
public class Metadata {
  
  public static final int NAME_LENGTH = 500;
  public static final int DESCRIPTION_LENGTH = 10000;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @URL
  @Column(nullable = false)
  private String identifier;

  @Column(nullable = false, length = NAME_LENGTH)
  private String name;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<Caption> caption;

  @URL
  private String contextUri;

  private String contextLanguage;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<Creator> creator;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<Contributor> contributor;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private ConditionsOfAccess conditionsOfAccess;

  @Column(length = DESCRIPTION_LENGTH)
  private String description;

  private String duration;

  private Boolean isAccessibleForFree;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<About> about;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<Media> encoding;
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Provider provider;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private License license;

  @URL
  private String image;
  private Integer imageWidth;
  private Integer imageHeight;

  private String dateCreated;
  private String datePublished;

  @ElementCollection
  @CollectionTable(name = "inLanguage")
  private List<String> inLanguage;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<LearningResourceType> learningResourceType;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<Audience> audience;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<MainEntityOfPage> mainEntityOfPage;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<Publisher> publisher;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "metadata_id", nullable = true)
  private List<SourceOrganization> sourceOrganization;

  @ElementCollection
  @CollectionTable(name = "types")
  private List<String> type;

  @ElementCollection
  @CollectionTable(name = "keywords")
  private List<String> keywords;

  @Column(nullable = false)
  private LocalDateTime dateModifiedInternal;

}
