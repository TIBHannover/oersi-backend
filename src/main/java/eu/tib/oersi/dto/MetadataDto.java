package eu.tib.oersi.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Data transfer object for OER metadata.
 */
@Data
public class MetadataDto {
  private Long id;
  private List<AuthorDto> authors;
  private EducationalResourceDto educationalResource;
  private InstitutionDto institution;
  private DidacticsDto didactics;
  private String source;
  private LocalDateTime dateModifiedInternal;
}
