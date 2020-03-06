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
 * InstitutionDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-03-05T12:39:28.323+01:00")

public class InstitutionDto   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("ror")
  private String ror = null;

  public InstitutionDto id(Long id) {
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

  public InstitutionDto name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InstitutionDto ror(String ror) {
    this.ror = ror;
    return this;
  }

  /**
   * Get ror
   * @return ror
  **/
  @ApiModelProperty(value = "")


  public String getRor() {
    return ror;
  }

  public void setRor(String ror) {
    this.ror = ror;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InstitutionDto institution = (InstitutionDto) o;
    return Objects.equals(this.id, institution.id) &&
        Objects.equals(this.name, institution.name) &&
        Objects.equals(this.ror, institution.ror);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, ror);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InstitutionDto {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    ror: ").append(toIndentedString(ror)).append("\n");
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

