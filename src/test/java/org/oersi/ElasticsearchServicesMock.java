package org.oersi;

import org.oersi.repository.BackendConfigRepository;
import org.oersi.repository.EsMetadataRepository;
import org.oersi.service.MetadataService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class ElasticsearchServicesMock {
  @MockBean
  private EsMetadataRepository metadataRepository;
  @MockBean
  private MetadataService metadataService;
  @MockBean
  private BackendConfigRepository configRepository;
}
