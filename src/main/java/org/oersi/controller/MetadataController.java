package org.oersi.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.oersi.api.MetadataControllerApi;
import org.oersi.domain.Metadata;
import org.oersi.dto.MetadataDto;
import org.oersi.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that handles crud requests to the OER index.
 */
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataController implements MetadataControllerApi {

  private final @NonNull MetadataService metadataService;

  private final @NonNull ModelMapper modelMapper;

  private Metadata convertToEntity(final MetadataDto dto) {
    return modelMapper.map(dto, Metadata.class);
  }

  private MetadataDto convertToDto(final Metadata entity) {
    return modelMapper.map(entity, MetadataDto.class);
  }

  /**
   * Retrieve the {@link Metadata} with the given id.
   *
   * @param id id of the data
   * @return data
   */
  @Override
  public ResponseEntity<MetadataDto> findById(final Long id) {
    Metadata metadata = metadataService.findById(id);
    if (metadata == null) {
      return getResponseForNonExistingData(id);
    }
    return ResponseEntity.ok(convertToDto(metadata));
  }

  private ResponseEntity<MetadataDto> getResponseForNonExistingData(final Long id) {
    log.debug("Metadata with id {} does not exist!", id);
    return ResponseEntity.badRequest().build();
  }

  /**
   * Create or update an {@link Metadata}
   *
   * @param metadataDto data to create or update
   * @return response
   */
  @Override
  public ResponseEntity<MetadataDto> createOrUpdate(@RequestBody final MetadataDto metadataDto) {
    Metadata metadata = metadataService.createOrUpdate(convertToEntity(metadataDto));
    log.debug("Created/Updated Metadata: {}", metadata);
    return ResponseEntity.ok(convertToDto(metadata));
  }

  /**
   * Update an {@link Metadata}.
   *
   * @param id id of the data
   * @param metadataDto data to update
   * @return response
   */
  @Override
  public ResponseEntity<MetadataDto> update(@PathVariable final Long id,
      @RequestBody final MetadataDto metadataDto) {
    Metadata metadata = metadataService.findById(id);
    if (metadata == null) {
      return getResponseForNonExistingData(id);
    }
    metadata = metadataService.createOrUpdate(convertToEntity(metadataDto));
    log.debug("Updated Metadata: {}", metadata);
    return ResponseEntity.ok(convertToDto(metadata));
  }

  /**
   * Delete an {@link Metadata}.
   *
   * @param id id of the data to delete
   * @return response
   */
  @Override
  public ResponseEntity<MetadataDto> delete(@PathVariable final Long id) {
    Metadata metadata = metadataService.findById(id);
    if (metadata == null) {
      return getResponseForNonExistingData(id);
    }
    metadataService.delete(metadata);
    return ResponseEntity.ok().build();
  }
  
  @Override
  public ResponseEntity<Void> deleteAll() {
    metadataService.deleteAll();
    return ResponseEntity.ok().build();
  }

}
