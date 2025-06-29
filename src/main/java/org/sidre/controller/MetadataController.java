package org.sidre.controller;

import jakarta.validation.ValidationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.modelmapper.MappingException;
import org.sidre.api.MetadataControllerApi;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.BackendMetadataEnrichment;
import org.sidre.dto.*;
import org.sidre.service.MetadataEnrichmentService;
import org.sidre.service.MetadataFieldService;
import org.sidre.service.MetadataService;
import org.sidre.service.MetadataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
  private final @NonNull MetadataEnrichmentService metadataEnrichmentService;
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
  public ResponseEntity<Map<String, Object>> update(final String id, @RequestBody final Map<String, Object> metadataDto) {
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
  public ResponseEntity<Map<String, Object>> delete(final String id, Boolean updatePublic) {
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

  @Override
  public ResponseEntity<List<MetadataEnrichmentDto>> findMetadataEnrichments(String metadataId, Integer page, Integer size) {
    List<BackendMetadataEnrichment> enrichments;
    if (metadataId != null) {
      enrichments = metadataEnrichmentService.findMetadataEnrichmentsByRestrictionMetadataId(metadataId);
      if (enrichments.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
    } else {
      enrichments = metadataEnrichmentService.findMetadataEnrichments(page, size);
    }
    List<MetadataEnrichmentDto> enrichmentsDto = enrichments.stream().map(enrichment -> {
      MetadataEnrichmentDto enrichmentDto = new MetadataEnrichmentDto();
      enrichmentDto.setId(enrichment.getId());
      enrichmentDto.setRestrictionMetadataId(enrichment.getRestrictionMetadataId());
      enrichmentDto.setOnlyExtended(enrichment.getOnlyExtended());
      enrichmentDto.setFieldValues(enrichment.getFieldValues());
      return enrichmentDto;
    }).toList();
    return ResponseEntity.ok(enrichmentsDto);
  }

  @Override
  public ResponseEntity<Void> createOrUpdateMetadataEnrichments(List<MetadataEnrichmentDto> metadataEnrichmentDtos, Boolean updateMetadata) {
    List<BackendMetadataEnrichment> metadataEnrichments = metadataEnrichmentDtos.stream().map(enrichmentDto -> {
      BackendMetadataEnrichment enrichment = new BackendMetadataEnrichment();
      enrichment.setRestrictionMetadataId(enrichmentDto.getRestrictionMetadataId());
      enrichment.setOnlyExtended(enrichmentDto.getOnlyExtended());
      enrichment.setFieldValues(enrichmentDto.getFieldValues());
      return enrichment;
    }).toList();
    metadataEnrichmentService.createOrUpdate(metadataEnrichments);

    if (Boolean.TRUE.equals(updateMetadata)) {
      List<BackendMetadata> dataToUpdate = new ArrayList<>();
      metadataEnrichments.stream()
          .filter(e -> e.getRestrictionMetadataId() != null)
          .forEach(enrichment -> {
        BackendMetadata metadata = metadataService.findById(enrichment.getRestrictionMetadataId());
        if (metadata != null) {
          metadataEnrichmentService.addMetadataEnrichments(metadata, enrichment);
          dataToUpdate.add(metadata);
        }
      });
      if (!dataToUpdate.isEmpty()) {
        metadataService.persist(dataToUpdate);
      }
    }
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> deleteManyMetadataEnrichments(List<String> enrichmentIds) {
    metadataEnrichmentService.deleteByIds(enrichmentIds);
    return ResponseEntity.ok().build();
  }

}
