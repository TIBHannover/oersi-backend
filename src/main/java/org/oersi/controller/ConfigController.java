package org.oersi.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.oersi.api.ConfigControllerApi;
import org.oersi.domain.BackendConfig;
import org.oersi.dto.ConfigDto;
import org.oersi.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigController implements ConfigControllerApi {

  private final @NonNull ConfigService configService;
  private final @NonNull ModelMapper modelMapper;

  @Override
  public ResponseEntity<Void> updateMetadataConfig(ConfigDto body) {
    log.debug("config update {}", body);
    configService.updateMetadataConfig(modelMapper.map(body, BackendConfig.class));
    return ResponseEntity.ok().build();
  }

}
