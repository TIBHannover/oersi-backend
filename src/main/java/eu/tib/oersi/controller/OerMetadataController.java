package eu.tib.oersi.controller;

import eu.tib.oersi.domain.OerMetadata;
import eu.tib.oersi.dto.OerMetadataDto;
import eu.tib.oersi.service.OerMetadataService;
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
@RequestMapping(value = OerMetadataController.BASE_PATH)
@Slf4j
public class OerMetadataController {

  /** base path of the {@link OerMetadataController} */
  public static final String BASE_PATH = "/api/metadata";

  @Autowired
  private OerMetadataService oerMetadataService;

  @Autowired
  private ModelMapper modelMapper;

  private OerMetadata convertToEntity(final OerMetadataDto dto) {
    return modelMapper.map(dto, OerMetadata.class);
  }

  private OerMetadataDto convertToDto(final OerMetadata entity) {
    return modelMapper.map(entity, OerMetadataDto.class);
  }

  /**
   * Retrieve the {@link OerMetadata} with the given id.
   * @param id id of the data
   * @return data
   */
  @GetMapping("/{id}")
  public ResponseEntity<OerMetadataDto> findById(@PathVariable final Long id) {
    OerMetadata oerMetadata = oerMetadataService.findById(id);
    if (oerMetadata == null) {
      return getResponseForNonExistingData(id);
    }
    return ResponseEntity.ok(convertToDto(oerMetadata));
  }

  private ResponseEntity<OerMetadataDto> getResponseForNonExistingData(final Long id) {
    log.debug("OerMetadata with id {} does not exist!", id);
    return ResponseEntity.badRequest().build();
  }

  /**
   * Create or update an {@link OerMetadata}
   * @param oerMetadataDto data to create or update
   * @return response
   */
  @PostMapping
  public ResponseEntity<OerMetadataDto> createOrUpdate(
      @RequestBody final OerMetadataDto oerMetadataDto) {
    OerMetadata oerMetadata = oerMetadataService.createOrUpdate(convertToEntity(oerMetadataDto));
    log.debug("Created/Updated OerMetadata: {}", oerMetadata);
    return ResponseEntity.ok(convertToDto(oerMetadata));
  }

  /**
   * Update an {@link OerMetadata}.
   * @param id id of the data
   * @param oerMetadataDto data to update
   * @return response
   */
  @PutMapping("/{id}")
  public ResponseEntity<OerMetadataDto> update(@PathVariable final Long id,
      @RequestBody final OerMetadataDto oerMetadataDto) {
    OerMetadata oerMetadata = oerMetadataService.findById(id);
    if (oerMetadata == null) {
      return getResponseForNonExistingData(id);
    } else if (oerMetadataDto.getId() == null || !id.equals(oerMetadataDto.getId())) {
      log.debug("OerMetadata id not set properly: {} {}", id, oerMetadata.getId());
      return ResponseEntity.badRequest().build();
    }
    oerMetadata = oerMetadataService.createOrUpdate(convertToEntity(oerMetadataDto));
    log.debug("Updated OerMetadata: {}", oerMetadata);
    return ResponseEntity.ok(convertToDto(oerMetadata));
  }

  /**
   * Delete an {@link OerMetadata}.
   * @param id id of the data to delete
   * @return response
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<OerMetadataDto> delete(@PathVariable final Long id) {
    OerMetadata oerMetadata = oerMetadataService.findById(id);
    if (oerMetadata == null) {
      return getResponseForNonExistingData(id);
    }
    oerMetadataService.delete(oerMetadata);
    return ResponseEntity.ok().build();
  }

}
