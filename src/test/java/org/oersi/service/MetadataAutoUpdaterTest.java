package org.oersi.service;

import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.domain.BackendMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class MetadataAutoUpdaterTest {

  @Autowired
  private MetadataAutoUpdater metadataAutoUpdater;

  @Test
  void testProviderInfos() {
    BackendMetadata data = MetadataHelper.toMetadata(Map.of("id", "https://av.tib.eu/media/12345"));

    metadataAutoUpdater.initAutoUpdateInfo(data);
    assertThat(data.getAutoUpdateInfo().getProviderName()).isEqualTo("TIB AV-Portal");
    assertThat(data.getAutoUpdateInfo().getProviderUrl()).isEqualTo("https://av.tib.eu");
  }

  @Test
  void testEmbedUrl() {
    BackendMetadata data = MetadataHelper.toMetadata(Map.of("id", "https://av.tib.eu/media/12345"));

    metadataAutoUpdater.initAutoUpdateInfo(data);
    assertThat(data.getAutoUpdateInfo().getEmbedUrl()).isEqualTo("https://av.tib.eu/player/12345");
  }

}
