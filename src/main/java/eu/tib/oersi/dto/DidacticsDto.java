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
 * DidacticsDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-03-05T12:39:28.323+01:00")

public class DidacticsDto   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("audience")
  private String audience = null;

  @JsonProperty("educationalUse")
  private String educationalUse = null;

  @JsonProperty("interactivityType")
  private String interactivityType = null;

  @JsonProperty("timeRequired")
  private String timeRequired = null;

  public DidacticsDto id(Long id) {
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

  public DidacticsDto audience(String audience) {
    this.audience = audience;
    return this;
  }

  /**
   * Audience 
   * @return audience
  **/
  @ApiModelProperty(value = "Audience ")


  public String getAudience() {
    return audience;
  }

  public void setAudience(String audience) {
    this.audience = audience;
  }

  public DidacticsDto educationalUse(String educationalUse) {
    this.educationalUse = educationalUse;
    return this;
  }

  /**
   * Education  
   * @return educationalUse
  **/
  @ApiModelProperty(value = "Education  ")


  public String getEducationalUse() {
    return educationalUse;
  }

  public void setEducationalUse(String educationalUse) {
    this.educationalUse = educationalUse;
  }

  public DidacticsDto interactivityType(String interactivityType) {
    this.interactivityType = interactivityType;
    return this;
  }

  /**
   * IterActive Type 
   * @return interactivityType
  **/
  @ApiModelProperty(value = "IterActive Type ")


  public String getInteractivityType() {
    return interactivityType;
  }

  public void setInteractivityType(String interactivityType) {
    this.interactivityType = interactivityType;
  }

  public DidacticsDto timeRequired(String timeRequired) {
    this.timeRequired = timeRequired;
    return this;
  }

  /**
   * Time Required 
   * @return timeRequired
  **/
  @ApiModelProperty(value = "Time Required ")


  public String getTimeRequired() {
    return timeRequired;
  }

  public void setTimeRequired(String timeRequired) {
    this.timeRequired = timeRequired;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DidacticsDto didactics = (DidacticsDto) o;
    return Objects.equals(this.id, didactics.id) &&
        Objects.equals(this.audience, didactics.audience) &&
        Objects.equals(this.educationalUse, didactics.educationalUse) &&
        Objects.equals(this.interactivityType, didactics.interactivityType) &&
        Objects.equals(this.timeRequired, didactics.timeRequired);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, audience, educationalUse, interactivityType, timeRequired);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DidacticsDto {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    audience: ").append(toIndentedString(audience)).append("\n");
    sb.append("    educationalUse: ").append(toIndentedString(educationalUse)).append("\n");
    sb.append("    interactivityType: ").append(toIndentedString(interactivityType)).append("\n");
    sb.append("    timeRequired: ").append(toIndentedString(timeRequired)).append("\n");
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

