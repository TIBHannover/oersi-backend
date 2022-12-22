package org.oersi.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.oersi.domain.Metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage {@link Metadata}s.
 */
public interface MetadataService {

  @Getter
  @Setter
  @ToString
  @RequiredArgsConstructor
  class MetadataUpdateResult {
    private final Metadata metadata;
    private Boolean success = true;
    private List<String> messages = new ArrayList<>();
    public void addMessages(List<String> messages) {
      this.messages.addAll(messages);
    }
  }

  /**
   * Create or update the given {@link Metadata}.
   *
   * @param metadata metadata
   * @return updated metadata
   */
  MetadataUpdateResult createOrUpdate(Metadata metadata);

  /**
   * Create or update the given {@link Metadata}s.
   *
   * @param records list of metadata
   * @return updated metadata
   */
  List<MetadataUpdateResult> createOrUpdate(List<Metadata> records);

  /**
   * Delete the given {@link Metadata}.
   *
   * @param metadata metadata
   */
  void delete(Metadata metadata);

  /**
   * Delete all existing {@link Metadata}.
   */
  void deleteAll();

  /**
   * Delete existing {@link Metadata} that match the given provider name.
   *
   * @param providerName provider name
   */
  void deleteByProviderName(String providerName);

  /**
   * Delete the {@link org.oersi.domain.MainEntityOfPage} identified by the given id. Also delete the related {@link Metadata} if the mainEntityOfPage-List is empty afterwards.
   * @param mainEntityOfPageId identifier of the MainEntityOfPage
   */
  boolean deleteMainEntityOfPageByIdentifier(String mainEntityOfPageId);

  /**
   * Retrieve {@link Metadata} for the given id.
   *
   * @param id id
   * @return data
   */
  Metadata findById(Long id);

  List<Metadata> findByMainEntityOfPageId(final String mainEntityOfPageIdentifier);

}
