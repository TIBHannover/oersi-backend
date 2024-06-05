package org.sidre.service;

import org.sidre.domain.BackendConfig;

public interface ConfigService {

  void updateMetadataConfig(BackendConfig backendConfig);

  BackendConfig getMetadataConfig();

}
