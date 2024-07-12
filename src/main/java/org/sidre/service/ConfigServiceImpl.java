package org.sidre.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.BackendConfig;
import org.sidre.repository.BackendConfigRepository;
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
    updateFieldPropertiesWithDefaultProperties(backendConfig);
    currentConfig = backendConfigRepository.createOrUpdate(backendConfig);
    log.info("using new config {}", currentConfig);
  }

  private void updateFieldPropertiesWithDefaultProperties(BackendConfig backendConfig) {
    if (backendConfig != null && backendConfig.getFieldProperties() != null && backendConfig.getDefaultFieldProperties() != null) {
      for (BackendConfig.FieldProperties fieldProperties : backendConfig.getFieldProperties()) {
        if (fieldProperties.getVocabItemIdentifierField() == null) {
          fieldProperties.setVocabItemIdentifierField(backendConfig.getDefaultFieldProperties().getVocabItemIdentifierField());
        }
        if (fieldProperties.getVocabItemLabelField() == null) {
          fieldProperties.setVocabItemLabelField(backendConfig.getDefaultFieldProperties().getVocabItemLabelField());
        }
      }
    }
  }

  @Override
  public BackendConfig getMetadataConfig() {
    if (currentConfig == null) {
      loadConfigFromRepo();
    }
    return currentConfig;
  }

  private void loadConfigFromRepo() {
    currentConfig = backendConfigRepository.findById("search_index_backend_config").orElse(null);
  }

}
