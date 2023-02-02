package org.oersi.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.oersi.domain.BackendMetadata;

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

  List<BackendMetadata> findByMainEntityOfPageId(final String mainEntityOfPageId);

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
   * Delete the mainEntityOfPage that match the given provider name. Also delete the related {@link BackendMetadata} if the mainEntityOfPage-List is empty afterwards.
   *
   * @param providerName provider name
   */
  void deleteMainEntityOfPageByProviderName(String providerName, boolean updatePublicIndices);

  /**
   * Delete the mainEntityOfPage identified by the given id. Also delete the related {@link BackendMetadata} if the mainEntityOfPage-List is empty afterwards.
   * @param mainEntityOfPageId identifier of the MainEntityOfPage
   */
  boolean deleteMainEntityOfPageByIdentifier(String mainEntityOfPageId, boolean updatePublicIndices);

  /**
   * Initializes the elasticsearch mapping for the {@link BackendMetadata} index.
   */
  void initIndexMapping();

}
