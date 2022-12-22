package org.oersi.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.oersi.api.MetadataControllerApi;
import org.oersi.domain.Metadata;
import org.oersi.dto.MetadataBulkDeleteDto;
import org.oersi.dto.MetadataBulkUpdateResponseDto;
import org.oersi.dto.MetadataBulkUpdateResponseMessagesDto;
import org.oersi.dto.MetadataDto;
import org.oersi.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

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
  private List<Metadata> convertToEntity(final List<MetadataDto> dtos) {
    return dtos.stream().map(this::convertToEntity).collect(Collectors.toList());
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
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(MappingException.class)
  public ResponseEntity<String> handleMappingException(final MappingException e) {
    return ControllerUtil.handleMappingException(e);
  }
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<String> handleMappingException(final ValidationException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleMappingException(final IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }

  /**
   * Create or update an {@link Metadata}
   *
   * @param metadataDto data to create or update
   * @return response
   */
  @Override
  public ResponseEntity<MetadataDto> createOrUpdate(@RequestBody final MetadataDto metadataDto) {
    MetadataService.MetadataUpdateResult result = metadataService.createOrUpdate(convertToEntity(metadataDto));
    if (Boolean.FALSE.equals(result.getSuccess())) {
      throw new IllegalArgumentException(String.join(", ", result.getMessages()));
    }
    log.debug("Created/Updated Metadata: {}", result.getMetadata());
    return ResponseEntity.ok(convertToDto(result.getMetadata()));
  }

  /**
   * Create or update many {@link Metadata}
   *
   * @param records data to create or update
   * @return response
   */
  @Override
  public ResponseEntity<MetadataBulkUpdateResponseDto> createOrUpdateMany(@RequestBody final List<MetadataDto> records) {
    List<MetadataService.MetadataUpdateResult> results = metadataService.createOrUpdate(convertToEntity(records));
    List<MetadataService.MetadataUpdateResult> failures = results.stream().filter(r -> !r.getSuccess()).collect(Collectors.toList());
    MetadataBulkUpdateResponseDto response = new MetadataBulkUpdateResponseDto();
    response.setSuccess(results.size() - failures.size());
    response.setFailed(failures.size());
    response.setMessages(failures.stream().map(r -> {
      MetadataBulkUpdateResponseMessagesDto responseMessagesDto = new MetadataBulkUpdateResponseMessagesDto();
      responseMessagesDto.setRecordId(r.getMetadata().getIdentifier());
      responseMessagesDto.setMessages(r.getMessages());
      return responseMessagesDto;
    }).collect(Collectors.toList()));
    log.info("Created/Updated {} records. Success: {}, failures: {}", results.size(), response.getSuccess(), response.getFailed());
    return ResponseEntity.ok(response);
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
    MetadataService.MetadataUpdateResult result = metadataService.createOrUpdate(convertToEntity(metadataDto));
    if (Boolean.FALSE.equals(result.getSuccess())) {
      throw new IllegalArgumentException(String.join(", ", result.getMessages()));
    }
    log.debug("Updated Metadata: {}", result.getMetadata());
    return ResponseEntity.ok(convertToDto(result.getMetadata()));
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

  @Override
  public ResponseEntity<Void> deleteMany(MetadataBulkDeleteDto body) {
    if (body.getProviderName() != null) {
      metadataService.deleteByProviderName(body.getProviderName());
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.badRequest().build();
  }

  @Override
  public ResponseEntity<Void> deleteMainEntityOfPage(String id) {
    String mainEntityOfPageId = new String(Base64.getUrlDecoder().decode(id.getBytes(StandardCharsets.UTF_8)));
    if (!new UrlValidator().isValid(mainEntityOfPageId)) {
      return ResponseEntity.badRequest().build();
    }
    if (!metadataService.deleteMainEntityOfPageByIdentifier(mainEntityOfPageId)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().build();
  }

}
