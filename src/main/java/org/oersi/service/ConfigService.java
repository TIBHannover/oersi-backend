package org.oersi.service;

import org.oersi.domain.BackendConfig;

public interface ConfigService {

  void updateMetadataConfig(BackendConfig backendConfig);

  BackendConfig getMetadataConfig();

}
