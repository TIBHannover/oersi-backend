package org.oersi.service;

import org.junit.jupiter.api.Test;
import org.oersi.domain.About;
import org.oersi.domain.Media;
import org.oersi.domain.Metadata;
import org.oersi.domain.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MetadataAutoUpdaterTest {

  @Autowired
  private MetadataAutoUpdater metadataAutoUpdater;

  private Metadata newMetadata() {
    Metadata metadata = new Metadata();
    metadata.setIdentifier("https://www.test.de");
    return metadata;
  }

  @Test
  void testProviderInfos() {
    Metadata data = newMetadata();
    data.setIdentifier("https://av.tib.eu/media/12345");

    String testName = "Testname";
    String testIdentifier = "https://test.id";
    Provider provider = new Provider();
    provider.setName(testName);
    provider.setIdentifier(testIdentifier);

    data.setProvider(provider);

    metadataAutoUpdater.addMissingInfos(data);
    assertThat(data.getProvider().getName()).isEqualTo(testName);
    assertThat(data.getProvider().getIdentifier()).isEqualTo(testIdentifier);

    provider.setIdentifier(null);
    metadataAutoUpdater.addMissingInfos(data);
    assertThat(data.getProvider().getName()).isEqualTo(testName);
    assertThat(data.getProvider().getIdentifier()).isEqualTo("https://av.tib.eu");

    provider.setName(null);
    metadataAutoUpdater.addMissingInfos(data);
    assertThat(data.getProvider().getName()).isEqualTo("TIB AV-Portal");
    assertThat(data.getProvider().getIdentifier()).isEqualTo("https://av.tib.eu");

    data.setProvider(null);
    metadataAutoUpdater.addMissingInfos(data);
    assertThat(data.getProvider().getName()).isEqualTo("TIB AV-Portal");
    assertThat(data.getProvider().getIdentifier()).isEqualTo("https://av.tib.eu");
  }

  @Test
  void testEmbedUrl() {
    Metadata data = newMetadata();
    data.setIdentifier("https://av.tib.eu/media/12345");

    Media videoEncoding = new Media();
    videoEncoding.setEmbedUrl("https://embed.url");
    data.setEncoding(new ArrayList<>(List.of(videoEncoding)));
    metadataAutoUpdater.addMissingInfos(data);
    Media resultVideoEncoding = data.getEncoding().stream().filter(e -> e.getEmbedUrl() != null).findAny().get();
    assertThat(resultVideoEncoding.getEmbedUrl()).isEqualTo("https://embed.url");

    Media otherEncoding = new Media();
    otherEncoding.setEncodingFormat("image/png");
    data.setEncoding(new ArrayList<>(List.of(otherEncoding)));
    metadataAutoUpdater.addMissingInfos(data);
    resultVideoEncoding = data.getEncoding().stream().filter(e -> e.getEmbedUrl() != null).findAny().get();
    assertThat(resultVideoEncoding.getEmbedUrl()).isEqualTo("https://av.tib.eu/player/12345");

    data.setEncoding(null);
    metadataAutoUpdater.addMissingInfos(data);
    resultVideoEncoding = data.getEncoding().stream().filter(e -> e.getEmbedUrl() != null).findAny().get();
    assertThat(resultVideoEncoding.getEmbedUrl()).isEqualTo("https://av.tib.eu/player/12345");
  }

  @Test
  void testAddParentItemsForHierarchicalVocab() {
    Metadata data = newMetadata();
    About about = new About();
    about.setIdentifier("https://w3id.org/kim/hochschulfaechersystematik/n009");
    data.setAbout(new ArrayList<>(List.of(about)));
    metadataAutoUpdater.setFeatureAddMissingParentItems(true);
    metadataAutoUpdater.addMissingInfos(data);

    assertThat(data.getAbout()).hasSize(3);
  }
}
