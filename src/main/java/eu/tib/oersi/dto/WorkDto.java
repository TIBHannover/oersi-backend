package eu.tib.oersi.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Represents the description of the oer material.
 */
@Data
public class WorkDto {
  private Long id;
  private String name;
  private String description;
  private String subject;
  private List<String> keywords;
  private String license;
  private String url;
  private String inLanguage;
  private String learningResourceType;
  private String version;
  private LocalDateTime dateCreated;
  private LocalDateTime dateLastUpdated;
  private LocalDateTime datePublished;
  private String identifier;
  private String thumbnailUrl;
}
