package org.oersi.repository;

import java.util.List;
import org.oersi.domain.Metadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Metadata}.
 */
public interface MetadataRepository extends JpaRepository<Metadata, Long> {

  /**
   * Find the {@link Metadata} that matches the given url.
   *
   * @param url url
   * @return metadata
   */
  List<Metadata> findByIdentifier(String url);

  /**
   * Find the {@link Metadata} that contain the {@link org.oersi.domain.MainEntityOfPage} matching the given url.
   * @param mainEntityOfPageIdentifier identifier of the MainEntityOfPage
   * @return metadata
   */
  List<Metadata> findByMainEntityOfPageIdentifier(String mainEntityOfPageIdentifier);

  List<Metadata> findByMainEntityOfPageProviderNameAndIdGreaterThanOrderByIdAsc(String providerName, Long lastId, Pageable pageable);

}
