package org.oersi.controller;

import jakarta.validation.ValidationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.modelmapper.MappingException;
import org.oersi.api.MetadataControllerApi;
import org.oersi.domain.BackendMetadata;
import org.oersi.dto.MetadataBulkUpdateResponseDto;
import org.oersi.dto.MetadataBulkUpdateResponseMessagesDto;
import org.oersi.dto.MetadataSourceBulkDeleteDto;
import org.oersi.service.MetadataFieldService;
import org.oersi.service.MetadataService;
import org.oersi.service.MetadataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Controller that handles crud requests to the search index.
 */
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataController implements MetadataControllerApi {

  private final @NonNull MetadataService metadataService;
  private final @NonNull MetadataFieldService metadataFieldService;
  private final @NonNull MetadataValidator metadataValidator;

  /**
   * Retrieve the metadata with the given id.
   *
   * @param id id of the data
   * @return data
   */
  @Override
  public ResponseEntity<Map<String, Object>> findById(final String id) {
    BackendMetadata metadata = metadataService.findById(id);
    if (metadata == null) {
      return getResponseForNonExistingData(id);
    }
    return ResponseEntity.ok(metadata.getData());
  }

  private ResponseEntity<Map<String, Object>> getResponseForNonExistingData(final String id) {
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

  @Override
  public ResponseEntity<Map<String, Object>> createOrUpdate(Map<String, Object> body) {
    if (!metadataValidator.validateBaseFields(body).isValid()) {
      return ResponseEntity.badRequest().build();
    }
    BackendMetadata metadata = metadataFieldService.toMetadata(body);
    MetadataService.MetadataUpdateResult result = metadataService.createOrUpdate(metadata);
    if (Boolean.FALSE.equals(result.getSuccess())) {
      throw new IllegalArgumentException(String.join(", ", result.getMessages()));
    }
    log.debug("Created/Updated Metadata: {}", result.getMetadata());
    return ResponseEntity.ok(result.getMetadata().getData());
  }

  /**
   * Create or update many {@link BackendMetadata}
   *
   * @param records data to create or update
   * @return response
   */
  @Override
  public ResponseEntity<MetadataBulkUpdateResponseDto> createOrUpdateMany(@RequestBody final List<Map<String, Object>> records) {
    if (records.stream().anyMatch(r -> !metadataValidator.validateBaseFields(r).isValid())) {
      return ResponseEntity.badRequest().build();
    }
    List<BackendMetadata> backendMetadata = records.stream().map(metadataFieldService::toMetadata).toList();
    List<MetadataService.MetadataUpdateResult> results = metadataService.createOrUpdate(backendMetadata);
    List<MetadataService.MetadataUpdateResult> failures = results.stream().filter(r -> !r.getSuccess()).toList();
    MetadataBulkUpdateResponseDto response = new MetadataBulkUpdateResponseDto();
    response.setSuccess(results.size() - failures.size());
    response.setFailed(failures.size());
    response.setMessages(failures.stream().map(r -> {
      MetadataBulkUpdateResponseMessagesDto responseMessagesDto = new MetadataBulkUpdateResponseMessagesDto();
      responseMessagesDto.setRecordId(r.getMetadata().getId());
      responseMessagesDto.setMessages(r.getMessages());
      return responseMessagesDto;
    }).toList());
    log.info("Created/Updated {} records. Success: {}, failures: {}", results.size(), response.getSuccess(), response.getFailed());
    return ResponseEntity.ok(response);
  }

  /**
   * Update existing Metadata.
   *
   * @param id id of the data
   * @param metadataDto data to update
   * @return response
   */
  @Override
  public ResponseEntity<Map<String, Object>> update(@PathVariable final String id, @RequestBody final Map<String, Object> metadataDto) {
    if (!metadataValidator.validateBaseFields(metadataDto).isValid()) {
      return ResponseEntity.badRequest().build();
    }
    BackendMetadata metadata = metadataService.findById(id);
    if (metadata == null) {
      return getResponseForNonExistingData(id);
    }
    MetadataService.MetadataUpdateResult result = metadataService.createOrUpdate(metadataFieldService.toMetadata(metadataDto));
    if (Boolean.FALSE.equals(result.getSuccess())) {
      throw new IllegalArgumentException(String.join(", ", result.getMessages()));
    }
    log.debug("Updated Metadata: {}", result.getMetadata());
    return ResponseEntity.ok(result.getMetadata().getData());
  }

  /**
   * Delete an {@link BackendMetadata}.
   *
   * @param id id of the data to delete
   * @return response
   */
  @Override
  public ResponseEntity<Map<String, Object>> delete(@PathVariable final String id, Boolean updatePublic) {
    BackendMetadata metadata = metadataService.findById(id);
    if (metadata == null) {
      return getResponseForNonExistingData(id);
    }
    metadataService.delete(metadata, updatePublic);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> deleteAll(Boolean updatePublic) {
    metadataService.deleteAll(updatePublic);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> deleteManySourceInformation(MetadataSourceBulkDeleteDto body, Boolean updatePublic) {
    if (body.getQueryName() != null && body.getQueryParam() != null) {
      if (!metadataService.deleteSourceEntriesByNamedQuery(body.getQueryName(), body.getQueryParam(), updatePublic)) {
        return ResponseEntity.badRequest().build();
      }
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.badRequest().build();
  }

  @Override
  public ResponseEntity<Void> deleteSourceInformation(String id, Boolean updatePublic) {
    String sourceInfoIdentifier = new String(Base64.getUrlDecoder().decode(id.getBytes(StandardCharsets.UTF_8)));
    if (!new UrlValidator().isValid(sourceInfoIdentifier)) {
      return ResponseEntity.badRequest().build();
    }
    if (!metadataService.deleteSourceEntryByIdentifier(sourceInfoIdentifier, updatePublic)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().build();
  }

}
