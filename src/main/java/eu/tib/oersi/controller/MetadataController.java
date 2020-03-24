package eu.tib.oersi.controller;

import eu.tib.oersi.MetadataDto;
import eu.tib.oersi.api.MetadataControllerApi;
import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
/**
 * Controller that handles crud requests to the OER index.
 */
@RestController
@Slf4j
public class MetadataController implements MetadataControllerApi {

  private final MetadataService metadataService;

  private final ModelMapper modelMapper;

  public MetadataController(MetadataService metadataService, ModelMapper modelMapper) {
    this.metadataService = metadataService;
    this.modelMapper = modelMapper;
  }

  private Metadata convertToEntity(final MetadataDto dto) {
    return modelMapper.map(dto, Metadata.class);
  }

  private MetadataDto convertToDto(final Metadata entity) {
    return modelMapper.map(entity, MetadataDto.class);
  }

  /**
   * Retrieve the {@link Metadata} with the given id.
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

  /**
   * @param id
   * @return MetadataDto
   */
  private ResponseEntity<MetadataDto> getResponseForNonExistingData(final Long id) {
    log.debug("Metadata with id {} does not exist!", id);
    return ResponseEntity.badRequest().build();
  }

  /**
   * Create or update an {@link Metadata}
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
    } else if (metadataDto.getId() == null || !id.equals(metadataDto.getId())) {
      log.debug("Metadata id not set properly: {} {}", id, metadata.getId());
      return ResponseEntity.badRequest().build();
    }
    metadata = metadataService.createOrUpdate(convertToEntity(metadataDto));
    log.debug("Updated Metadata: {}", metadata);
    return ResponseEntity.ok(convertToDto(metadata));
  }

  /**
   * Delete an {@link Metadata}.
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

}
