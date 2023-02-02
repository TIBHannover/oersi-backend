package org.oersi;

import lombok.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.oersi.domain.BackendConfig;
import org.oersi.domain.BackendMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(ElasticsearchContainerTest.ElasticsearchBackendConfig.class)
@Testcontainers
public abstract class ElasticsearchContainerTest {

  private static final String IMAGE_NAME = "docker.elastic.co/elasticsearch/elasticsearch:7.17.7";

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Container
  private static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(IMAGE_NAME)
    .withExposedPorts(9200)
    .withEnv(Map.of(
        "xpack.security.enabled", "false",
        "ES_JAVA_OPTS", "-Xms2g -Xmx2g"
      )
    );

  @TestConfiguration
  @PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
  public static class ElasticsearchBackendConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.oersi_backend_manager_username}")
    private String backendManagerUsername;

    @Value("${elasticsearch.oersi_backend_manager_password}")
    private String backendManagerPassword;

    @Override
    public @NonNull ClientConfiguration clientConfiguration() {
      final int elasticsearchPort = elasticsearchContainer.getMappedPort(9200);
      return ClientConfiguration.builder()
        .connectedTo(elasticsearchHost + ":" + elasticsearchPort)
        .withBasicAuth(backendManagerUsername, backendManagerPassword)
        .build();
    }
  }

  @BeforeAll
  static void setUp() {
    elasticsearchContainer.start();
  }

  @BeforeEach
  void testIsContainerRunning() {
    assertTrue(elasticsearchContainer.isRunning());
  }

  @AfterEach
  void recreateIndices() {
    recreateIndex(BackendMetadata.class);
    recreateIndex(BackendConfig.class);
  }
  private void recreateIndex(Class<?> clazz) {
    IndexOperations indexOperations = elasticsearchOperations.indexOps(clazz);
    indexOperations.delete();
    indexOperations.create();
  }

  @AfterAll
  static void destroy() {
    elasticsearchContainer.stop();
  }

}
