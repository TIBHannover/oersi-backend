package eu.tib.oersi.repository;

import eu.tib.oersi.domain.OerMetadata;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link OerMetadata}.
 */
public interface OerMetadataRepository extends JpaRepository<OerMetadata, Long> {

  /**
   * Find the {@link OerMetadata} that matches the given url.
   * @param url url
   * @return metadata
   */
  List<OerMetadata> findByWorkUrl(String url);

}
