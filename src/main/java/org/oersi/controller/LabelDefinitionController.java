package org.oersi.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.oersi.api.LabelDefinitionControllerApi;
import org.oersi.domain.LabelDefinition;
import org.oersi.domain.LocalizedString;
import org.oersi.dto.LabelDefinitionDto;
import org.oersi.dto.LocalizedStringDto;
import org.oersi.service.LabelDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LabelDefinitionController implements LabelDefinitionControllerApi {

  private final @NonNull LabelDefinitionService labelDefinitionService;
  private final @NonNull ModelMapper modelMapper;

  private List<LabelDefinition> convertToEntity(final Map<String, LocalizedStringDto> dto) {
    return dto.entrySet().stream().map(e -> {
      LabelDefinition definition = new LabelDefinition();
      definition.setIdentifier(e.getKey());
      if (e.getValue() != null) {
        LocalizedString labels = new LocalizedString();
        labels.setLocalizedStrings(e.getValue());
        definition.setLabel(labels);
      }
      return definition;
    }).collect(Collectors.toList());
  }

  private LabelDefinition convertToEntity(final LabelDefinitionDto dto) {
    return modelMapper.map(dto, LabelDefinition.class);
  }

  private LabelDefinitionDto convertToDto(final LabelDefinition entity) {
    return modelMapper.map(entity, LabelDefinitionDto.class);
  }

  @ExceptionHandler(MappingException.class)
  public ResponseEntity<String> handleMappingException(final MappingException e) {
    return ControllerUtil.handleMappingException(e);
  }
  
  private ResponseEntity<LabelDefinitionDto> getResponseForNonExistingData(final Long id) {
    log.debug("LabelDefinition with id {} does not exist!", id);
    return ResponseEntity.notFound().build();
  }

  @Override
  public ResponseEntity<LabelDefinitionDto> findById(final Long id) {
    LabelDefinition data = labelDefinitionService.findById(id);
    if (data == null) {
      return getResponseForNonExistingData(id);
    }
    return ResponseEntity.ok(convertToDto(data));
  }

  /**
   * Create or update an {@link LabelDefinition}
   *
   * @param labelDefinitionDto data to create or update
   * @return response
   */
  @Override
  public ResponseEntity<LabelDefinitionDto> createOrUpdate(@RequestBody final LabelDefinitionDto labelDefinitionDto) {
    LabelDefinition labelDefinition = labelDefinitionService.createOrUpdate(List.of(convertToEntity(labelDefinitionDto))).get(0);
    log.debug("Created/Updated labelDefinition: {}", labelDefinition);
    return ResponseEntity.ok(convertToDto(labelDefinition));
  }

  /**
   * Create or update multiple {@link LabelDefinition}s
   *
   * @param labelDefinitionsDto data to create or update
   * @return response
   */
  @Override
  public ResponseEntity<Void> createOrUpdateMany(@RequestBody final Map<String, LocalizedStringDto> labelDefinitionsDto) {
    List<LabelDefinition> labelDefinitions = convertToEntity(labelDefinitionsDto);
    labelDefinitionService.createOrUpdate(labelDefinitions);
    return ResponseEntity.ok().build();
  }

  /**
   * Update an {@link LabelDefinition}.
   *
   * @param id id of the data
   * @param labelDefinitionDto data to update
   * @return response
   */
  @Override
  public ResponseEntity<LabelDefinitionDto> update(@PathVariable final Long id,
                                            @RequestBody final LabelDefinitionDto labelDefinitionDto) {
    LabelDefinition labelDefinition = labelDefinitionService.findById(id);
    if (labelDefinition == null) {
      return getResponseForNonExistingData(id);
    }
    labelDefinition = labelDefinitionService.createOrUpdate(List.of(convertToEntity(labelDefinitionDto))).get(0);
    log.debug("Updated labelDefinition: {}", labelDefinition);
    return ResponseEntity.ok(convertToDto(labelDefinition));
  }

  /**
   * Delete an {@link LabelDefinition}.
   *
   * @param id id of the data to delete
   * @return response
   */
  @Override
  public ResponseEntity<LabelDefinitionDto> delete(@PathVariable final Long id) {
    LabelDefinition labelDefinitionDto = labelDefinitionService.findById(id);
    if (labelDefinitionDto == null) {
      return getResponseForNonExistingData(id);
    }
    labelDefinitionService.delete(labelDefinitionDto);
    return ResponseEntity.ok().build();
  }
}
