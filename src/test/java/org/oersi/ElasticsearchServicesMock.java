package org.oersi;

import org.oersi.repository.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@TestConfiguration
public class ElasticsearchServicesMock {
  @MockBean
  private MetadataRepository metadataRepository;
  @MockBean
  private BackendConfigRepository configRepository;
  @MockBean
  private VocabItemRepository vocabItemRepository;
  @MockBean
  private LabelRepository repository;
  @MockBean
  private ElasticsearchRequestLogRepository requestLogRepository;
  @MockBean
  private ElasticsearchOperations elasticsearchOperations;
  @MockBean
  private ElasticsearchStartupApplicationListener elasticsearchStartupApplicationListener;
}
