package org.oersi;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
public class ElasticsearchBackendConfig extends ElasticsearchConfiguration {

  @Value("${elasticsearch.host}")
  private String elasticsearchHost;

  @Value("${elasticsearch.port}")
  private int elasticsearchPort;

  @Value("${elasticsearch.search_index_backend_manager_username}")
  private String backendManagerUsername;

  @Value("${elasticsearch.search_index_backend_manager_password}")
  private String backendManagerPassword;

  @Override
  public @NonNull ClientConfiguration clientConfiguration() {
    return ClientConfiguration.builder()
      .connectedTo(elasticsearchHost + ":" + elasticsearchPort)
      .withBasicAuth(backendManagerUsername, backendManagerPassword)
      .build();
  }
}
