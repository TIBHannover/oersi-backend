package eu.tib.oersi.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * AuthorDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-02-28T15:26:38.321+01:00")

public class AuthorDto   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("orcid")
  private String orcid = null;

  @JsonProperty("gnd")
  private String gnd = null;

  @JsonProperty("givenName")
  private String givenName = null;

  @JsonProperty("familyName")
  private String familyName = null;

  public AuthorDto id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Id of Author
   * @return id
  **/
  @ApiModelProperty(value = "Id of Author")


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AuthorDto orcid(String orcid) {
    this.orcid = orcid;
    return this;
  }

  /**
   * Id of 
   * @return orcid
  **/
  @ApiModelProperty(value = "Id of ")


  public String getOrcid() {
    return orcid;
  }

  public void setOrcid(String orcid) {
    this.orcid = orcid;
  }

  public AuthorDto gnd(String gnd) {
    this.gnd = gnd;
    return this;
  }

  /**
   *  
   * @return gnd
  **/
  @ApiModelProperty(value = " ")


  public String getGnd() {
    return gnd;
  }

  public void setGnd(String gnd) {
    this.gnd = gnd;
  }

  public AuthorDto givenName(String givenName) {
    this.givenName = givenName;
    return this;
  }

  /**
   * First Name
   * @return givenName
  **/
  @ApiModelProperty(value = "First Name")


  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public AuthorDto familyName(String familyName) {
    this.familyName = familyName;
    return this;
  }

  /**
   * Family Name
   * @return familyName
  **/
  @ApiModelProperty(value = "Family Name")


  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthorDto author = (AuthorDto) o;
    return Objects.equals(this.id, author.id) &&
        Objects.equals(this.orcid, author.orcid) &&
        Objects.equals(this.gnd, author.gnd) &&
        Objects.equals(this.givenName, author.givenName) &&
        Objects.equals(this.familyName, author.familyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, orcid, gnd, givenName, familyName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorDto {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    orcid: ").append(toIndentedString(orcid)).append("\n");
    sb.append("    gnd: ").append(toIndentedString(gnd)).append("\n");
    sb.append("    givenName: ").append(toIndentedString(givenName)).append("\n");
    sb.append("    familyName: ").append(toIndentedString(familyName)).append("\n");
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

