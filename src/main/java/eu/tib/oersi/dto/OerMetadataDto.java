package eu.tib.oersi.dto;

import eu.tib.oersi.domain.Work;
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
  private Work work;
  private DidacticsDto didactics;
  private String source;
  private LocalDateTime dateModifiedInternal;
}
