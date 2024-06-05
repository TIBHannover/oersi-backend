package org.sidre.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.sidre.domain.BackendMetadata;

import java.util.ArrayList;
import java.util.List;

public interface MetadataService {
  @Getter
  @Setter
  @ToString
  @RequiredArgsConstructor
  class MetadataUpdateResult {
    private final BackendMetadata metadata;
    private Boolean success = true;
    private List<String> messages = new ArrayList<>();
    public void addMessages(List<String> messages) {
      this.messages.addAll(messages);
    }
  }

  MetadataUpdateResult createOrUpdate(BackendMetadata metadata);

  List<MetadataUpdateResult> createOrUpdate(List<BackendMetadata> metadata);

  /**
   * Retrieve {@link BackendMetadata} for the given id.
   *
   * @param id id
   * @return data
   */
  BackendMetadata findById(String id);

  /**
   * Delete the given {@link BackendMetadata}.
   *
   * @param metadata metadata
   */
  void delete(BackendMetadata metadata, boolean updatePublicIndices);

  /**
   * Delete all existing {@link BackendMetadata}.
   */
  void deleteAll(boolean updatePublicIndices);

  /**
   * Delete the source information that match the given search parameter. Also delete the related {@link BackendMetadata} if the source-information-List is empty afterwards.
   *
   * @param searchParams search parameter
   */
  boolean deleteSourceEntriesByNamedQuery(String queryName, String queryParam, boolean updatePublicIndices);

  /**
   * Delete the source information identified by the given id. Also delete the related {@link BackendMetadata} if the source-information-List is empty afterwards.
   * @param sourceEntryId identifier of the source information
   */
  boolean deleteSourceEntryByIdentifier(String sourceEntryId, boolean updatePublicIndices);

  /**
   * Initializes the elasticsearch mapping for the {@link BackendMetadata} index.
   */
  void initIndexMapping();

}
