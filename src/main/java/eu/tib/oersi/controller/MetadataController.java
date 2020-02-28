package eu.tib.oersi.controller;

import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.dto.MetadataDto;
import eu.tib.oersi.service.MetadataService;
import eu.tib.oersi.v2.MetadataControllerApi;
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

  /** base path of the {@link MetadataController} */
  public static final String BASE_PATH = "/api/metadata";

  private final ModelMapper modelMapper;
  private final MetadataService metadataService;

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

  @Override
  public ResponseEntity<MetadataDto> findById(Long id) {
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

  @Override
  public ResponseEntity<MetadataDto> createOrUpdate(
      @RequestBody final MetadataDto metadataDto) {
    Metadata metadata = metadataService.createOrUpdate(convertToEntity(metadataDto));
    log.debug("Created/Updated Metadata: {}", metadata);
    return ResponseEntity.ok(convertToDto(metadata));
  }

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
