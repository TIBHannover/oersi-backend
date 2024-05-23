package org.oersi.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.BackendConfig;
import org.oersi.repository.BackendConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigServiceImpl implements ConfigService {

  private final @NonNull BackendConfigRepository backendConfigRepository;

  private BackendConfig currentConfig = null;

  @Transactional
  @Override
  public void updateMetadataConfig(BackendConfig backendConfig) {
    log.debug("updating config {}", backendConfig);
    currentConfig = backendConfigRepository.createOrUpdate(backendConfig);
    log.info("using new config {}", currentConfig);
  }

  @Override
  public BackendConfig getMetadataConfig() {
    if (currentConfig == null) {
      loadConfigFromRepo();
    }
    return currentConfig;
  }

  private void loadConfigFromRepo() {
    currentConfig = backendConfigRepository.findById("oersi_backend_config").orElse(null);
  }

}
