package eu.tib.oersi.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * EducationalResourceDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-02-28T15:26:38.321+01:00")

public class EducationalResourceDto   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("dateCreated")
  private OffsetDateTime dateCreated = null;

  @JsonProperty("dateLastUpdated")
  private OffsetDateTime dateLastUpdated = null;

  @JsonProperty("datePublished")
  private OffsetDateTime datePublished = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("identifier")
  private String identifier = null;

  @JsonProperty("inLanguage")
  private String inLanguage = null;

  @JsonProperty("keywords")
  @Valid
  private List<String> keywords = null;

  @JsonProperty("learningResourceType")
  private String learningResourceType = null;

  @JsonProperty("license")
  private String license = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("subject")
  private String subject = null;

  @JsonProperty("thumbnailUrl")
  private String thumbnailUrl = null;

  @JsonProperty("url")
  private String url = null;

  @JsonProperty("version")
  private String version = null;

  public EducationalResourceDto id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public EducationalResourceDto dateCreated(OffsetDateTime dateCreated) {
    this.dateCreated = dateCreated;
    return this;
  }

  /**
   * Get dateCreated
   * @return dateCreated
  **/
  @ApiModelProperty(example = "2017-07-21T17:32:28Z", value = "")

  @Valid

  public OffsetDateTime getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(OffsetDateTime dateCreated) {
    this.dateCreated = dateCreated;
  }

  public EducationalResourceDto dateLastUpdated(OffsetDateTime dateLastUpdated) {
    this.dateLastUpdated = dateLastUpdated;
    return this;
  }

  /**
   * Get dateLastUpdated
   * @return dateLastUpdated
  **/
  @ApiModelProperty(example = "2017-07-21T17:32:28Z", value = "")

  @Valid

  public OffsetDateTime getDateLastUpdated() {
    return dateLastUpdated;
  }

  public void setDateLastUpdated(OffsetDateTime dateLastUpdated) {
    this.dateLastUpdated = dateLastUpdated;
  }

  public EducationalResourceDto datePublished(OffsetDateTime datePublished) {
    this.datePublished = datePublished;
    return this;
  }

  /**
   * Get datePublished
   * @return datePublished
  **/
  @ApiModelProperty(example = "2017-07-21T17:32:28Z", value = "")

  @Valid

  public OffsetDateTime getDatePublished() {
    return datePublished;
  }

  public void setDatePublished(OffsetDateTime datePublished) {
    this.datePublished = datePublished;
  }

  public EducationalResourceDto description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Long Text 
   * @return description
  **/
  @ApiModelProperty(value = "Long Text ")


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public EducationalResourceDto identifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  /**
   * Identify Number 
   * @return identifier
  **/
  @ApiModelProperty(value = "Identify Number ")


  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public EducationalResourceDto inLanguage(String inLanguage) {
    this.inLanguage = inLanguage;
    return this;
  }

  /**
   * Language 
   * @return inLanguage
  **/
  @ApiModelProperty(example = "English", value = "Language ")


  public String getInLanguage() {
    return inLanguage;
  }

  public void setInLanguage(String inLanguage) {
    this.inLanguage = inLanguage;
  }

  public EducationalResourceDto keywords(List<String> keywords) {
    this.keywords = keywords;
    return this;
  }

  public EducationalResourceDto addKeywordsItem(String keywordsItem) {
    if (this.keywords == null) {
      this.keywords = new ArrayList<>();
    }
    this.keywords.add(keywordsItem);
    return this;
  }

  /**
   * List Of Keywords 
   * @return keywords
  **/
  @ApiModelProperty(example = "[\"Math\",\"Sience\",\"Lesson\"]", value = "List Of Keywords ")


  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  public EducationalResourceDto learningResourceType(String learningResourceType) {
    this.learningResourceType = learningResourceType;
    return this;
  }

  /**
   * Learning Type
   * @return learningResourceType
  **/
  @ApiModelProperty(example = "PDF", value = "Learning Type")


  public String getLearningResourceType() {
    return learningResourceType;
  }

  public void setLearningResourceType(String learningResourceType) {
    this.learningResourceType = learningResourceType;
  }

  public EducationalResourceDto license(String license) {
    this.license = license;
    return this;
  }

  /**
   * License 
   * @return license
  **/
  @ApiModelProperty(value = "License ")


  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public EducationalResourceDto name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name
   * @return name
  **/
  @ApiModelProperty(value = "Name")


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EducationalResourceDto subject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Subject
   * @return subject
  **/
  @ApiModelProperty(value = "Subject")


  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public EducationalResourceDto thumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
    return this;
  }

  /**
   * Photo URL
   * @return thumbnailUrl
  **/
  @ApiModelProperty(value = "Photo URL")


  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public EducationalResourceDto url(String url) {
    this.url = url;
    return this;
  }

  /**
   * Url
   * @return url
  **/
  @ApiModelProperty(value = "Url")


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public EducationalResourceDto version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Version
   * @return version
  **/
  @ApiModelProperty(value = "Version")


  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EducationalResourceDto educationalResource = (EducationalResourceDto) o;
    return Objects.equals(this.id, educationalResource.id) &&
        Objects.equals(this.dateCreated, educationalResource.dateCreated) &&
        Objects.equals(this.dateLastUpdated, educationalResource.dateLastUpdated) &&
        Objects.equals(this.datePublished, educationalResource.datePublished) &&
        Objects.equals(this.description, educationalResource.description) &&
        Objects.equals(this.identifier, educationalResource.identifier) &&
        Objects.equals(this.inLanguage, educationalResource.inLanguage) &&
        Objects.equals(this.keywords, educationalResource.keywords) &&
        Objects.equals(this.learningResourceType, educationalResource.learningResourceType) &&
        Objects.equals(this.license, educationalResource.license) &&
        Objects.equals(this.name, educationalResource.name) &&
        Objects.equals(this.subject, educationalResource.subject) &&
        Objects.equals(this.thumbnailUrl, educationalResource.thumbnailUrl) &&
        Objects.equals(this.url, educationalResource.url) &&
        Objects.equals(this.version, educationalResource.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, dateCreated, dateLastUpdated, datePublished, description, identifier, inLanguage, keywords, learningResourceType, license, name, subject, thumbnailUrl, url, version);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EducationalResourceDto {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    dateCreated: ").append(toIndentedString(dateCreated)).append("\n");
    sb.append("    dateLastUpdated: ").append(toIndentedString(dateLastUpdated)).append("\n");
    sb.append("    datePublished: ").append(toIndentedString(datePublished)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
    sb.append("    inLanguage: ").append(toIndentedString(inLanguage)).append("\n");
    sb.append("    keywords: ").append(toIndentedString(keywords)).append("\n");
    sb.append("    learningResourceType: ").append(toIndentedString(learningResourceType)).append("\n");
    sb.append("    license: ").append(toIndentedString(license)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
    sb.append("    thumbnailUrl: ").append(toIndentedString(thumbnailUrl)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

