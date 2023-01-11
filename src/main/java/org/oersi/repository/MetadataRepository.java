package org.oersi.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.oersi.domain.Metadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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

  List<Metadata> findByRecordStatusInternalAndDateModifiedInternalBeforeAndIdGreaterThanOrderByIdAsc(Metadata.RecordStatus recordStatus, LocalDateTime dateModifiedUpperBound, Long lastId, Pageable pageable);

  @Query(value = "Update Metadata SET recordStatusInternal = :recordStatus, dateModifiedInternal = :dateModified")
  @Modifying
  int updateAllRecordStatusInternalAndDateModifiedInternal(Metadata.RecordStatus recordStatus, LocalDateTime dateModified);

}
