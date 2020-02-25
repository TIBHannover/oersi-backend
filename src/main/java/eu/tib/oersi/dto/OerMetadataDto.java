package eu.tib.oersi.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Data transfer object for OER metadata.
 */
@Data
public class OerMetadataDto {
  private Long id;
  private List<AuthorDto> authors;
  private WorkDto work;
  private InstitutionDto institution;
  private DidacticsDto didactics;
  private String source;
  private LocalDateTime dateModifiedInternal;
}
