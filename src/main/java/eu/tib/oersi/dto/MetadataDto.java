package eu.tib.oersi.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import eu.tib.oersi.dto.AuthorDto;
import eu.tib.oersi.dto.DidacticsDto;
import eu.tib.oersi.dto.EducationalResourceDto;
import eu.tib.oersi.dto.InstitutionDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * MetadataDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-02-28T15:26:38.321+01:00")

public class MetadataDto   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("authors")
  @Valid
  private List<AuthorDto> authors = null;

  @JsonProperty("dateModifiedInternal")
  private OffsetDateTime dateModifiedInternal = null;

  @JsonProperty("didactics")
  @Valid
  private List<DidacticsDto> didactics = null;

  @JsonProperty("educationalResource")
  @Valid
  private List<EducationalResourceDto> educationalResource = null;

  @JsonProperty("institution")
  @Valid
  private List<InstitutionDto> institution = null;

  @JsonProperty("source")
  private String source = null;

  public MetadataDto id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Id Object
   * @return id
  **/
  @ApiModelProperty(value = "Id Object")


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public MetadataDto authors(List<AuthorDto> authors) {
    this.authors = authors;
    return this;
  }

  public MetadataDto addAuthorsItem(AuthorDto authorsItem) {
    if (this.authors == null) {
      this.authors = new ArrayList<>();
    }
    this.authors.add(authorsItem);
    return this;
  }

  /**
   * Get authors
   * @return authors
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<AuthorDto> getAuthors() {
    return authors;
  }

  public void setAuthors(List<AuthorDto> authors) {
    this.authors = authors;
  }

  public MetadataDto dateModifiedInternal(OffsetDateTime dateModifiedInternal) {
    this.dateModifiedInternal = dateModifiedInternal;
    return this;
  }

  /**
   * Get dateModifiedInternal
   * @return dateModifiedInternal
  **/
  @ApiModelProperty(example = "2017-07-21T17:32:28Z", value = "")

  @Valid

  public OffsetDateTime getDateModifiedInternal() {
    return dateModifiedInternal;
  }

  public void setDateModifiedInternal(OffsetDateTime dateModifiedInternal) {
    this.dateModifiedInternal = dateModifiedInternal;
  }

  public MetadataDto didactics(List<DidacticsDto> didactics) {
    this.didactics = didactics;
    return this;
  }

  public MetadataDto addDidacticsItem(DidacticsDto didacticsItem) {
    if (this.didactics == null) {
      this.didactics = new ArrayList<>();
    }
    this.didactics.add(didacticsItem);
    return this;
  }

  /**
   * Get didactics
   * @return didactics
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<DidacticsDto> getDidactics() {
    return didactics;
  }

  public void setDidactics(List<DidacticsDto> didactics) {
    this.didactics = didactics;
  }

  public MetadataDto educationalResource(List<EducationalResourceDto> educationalResource) {
    this.educationalResource = educationalResource;
    return this;
  }

  public MetadataDto addEducationalResourceItem(EducationalResourceDto educationalResourceItem) {
    if (this.educationalResource == null) {
      this.educationalResource = new ArrayList<>();
    }
    this.educationalResource.add(educationalResourceItem);
    return this;
  }

  /**
   * Get educationalResource
   * @return educationalResource
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<EducationalResourceDto> getEducationalResource() {
    return educationalResource;
  }

  public void setEducationalResource(List<EducationalResourceDto> educationalResource) {
    this.educationalResource = educationalResource;
  }

  public MetadataDto institution(List<InstitutionDto> institution) {
    this.institution = institution;
    return this;
  }

  public MetadataDto addInstitutionItem(InstitutionDto institutionItem) {
    if (this.institution == null) {
      this.institution = new ArrayList<>();
    }
    this.institution.add(institutionItem);
    return this;
  }

  /**
   * Get institution
   * @return institution
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<InstitutionDto> getInstitution() {
    return institution;
  }

  public void setInstitution(List<InstitutionDto> institution) {
    this.institution = institution;
  }

  public MetadataDto source(String source) {
    this.source = source;
    return this;
  }

  /**
   * Get source
   * @return source
  **/
  @ApiModelProperty(value = "")


  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MetadataDto metadata = (MetadataDto) o;
    return Objects.equals(this.id, metadata.id) &&
        Objects.equals(this.authors, metadata.authors) &&
        Objects.equals(this.dateModifiedInternal, metadata.dateModifiedInternal) &&
        Objects.equals(this.didactics, metadata.didactics) &&
        Objects.equals(this.educationalResource, metadata.educationalResource) &&
        Objects.equals(this.institution, metadata.institution) &&
        Objects.equals(this.source, metadata.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, authors, dateModifiedInternal, didactics, educationalResource, institution, source);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MetadataDto {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    authors: ").append(toIndentedString(authors)).append("\n");
    sb.append("    dateModifiedInternal: ").append(toIndentedString(dateModifiedInternal)).append("\n");
    sb.append("    didactics: ").append(toIndentedString(didactics)).append("\n");
    sb.append("    educationalResource: ").append(toIndentedString(educationalResource)).append("\n");
    sb.append("    institution: ").append(toIndentedString(institution)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
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

