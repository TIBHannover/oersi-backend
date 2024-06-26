package org.sidre;

import org.sidre.repository.*;
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
  private OrganizationInfoRepository organizationInfoRepository;
  @MockBean
  private VocabItemRepository vocabItemRepository;
  @MockBean
  private ElasticsearchRequestLogRepository requestLogRepository;
  @MockBean
  private ElasticsearchOperations elasticsearchOperations;
  @MockBean
  private ElasticsearchStartupApplicationListener elasticsearchStartupApplicationListener;
}
