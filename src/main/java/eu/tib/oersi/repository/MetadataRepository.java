package eu.tib.oersi.repository;

import eu.tib.oersi.domain.Metadata;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Metadata}.
 */
public interface MetadataRepository extends JpaRepository<Metadata, Long> {

  /**
   * Find the {@link Metadata} that matches the given url.
   * @param url url
   * @return metadata
   */
  List<Metadata> findByEducationalResourceUrl(String url);

}
