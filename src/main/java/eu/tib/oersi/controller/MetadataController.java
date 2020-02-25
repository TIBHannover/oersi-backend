package eu.tib.oersi.controller;

import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.dto.MetadataDto;
import eu.tib.oersi.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that handles crud requests to the OER index.
 */
@RestController
@RequestMapping(value = MetadataController.BASE_PATH)
@Slf4j
public class MetadataController {

  /** base path of the {@link MetadataController} */
  public static final String BASE_PATH = "/api/metadata";

  @Autowired
  private MetadataService metadataService;

  @Autowired
  private ModelMapper modelMapper;

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
  @GetMapping("/{id}")
  public ResponseEntity<MetadataDto> findById(@PathVariable final Long id) {
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
   * @param metadataDto data to create or update
   * @return response
   */
  @PostMapping
  public ResponseEntity<MetadataDto> createOrUpdate(
      @RequestBody final MetadataDto metadataDto) {
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
  @PutMapping("/{id}")
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
  @DeleteMapping("/{id}")
  public ResponseEntity<MetadataDto> delete(@PathVariable final Long id) {
    Metadata metadata = metadataService.findById(id);
    if (metadata == null) {
      return getResponseForNonExistingData(id);
    }
    metadataService.delete(metadata);
    return ResponseEntity.ok().build();
  }

}
