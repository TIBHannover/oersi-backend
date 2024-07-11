package org.sidre.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.BackendMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class MetadataAutoUpdaterTest {

  @Autowired
  private MetadataAutoUpdater metadataAutoUpdater;
  @MockBean
  private VocabService vocabService;
  @MockBean
  private ConfigService configService;


  @BeforeEach
  void setup() {
    configService.updateMetadataConfig(null);
  }

  @Test
  void testProviderInfos() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(Map.of("id", "https://av.tib.eu/media/12345"), "id");

    metadataAutoUpdater.initAutoUpdateInfo(data);
    assertThat(data.getAutoUpdateInfo().getProviderName()).isEqualTo("TIB AV-Portal");
    assertThat(data.getAutoUpdateInfo().getProviderUrl()).isEqualTo("https://av.tib.eu");
  }

  @Test
  void testEmbedUrl() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(Map.of("id", "https://av.tib.eu/media/12345"), "id");

    metadataAutoUpdater.initAutoUpdateInfo(data);
    assertThat(data.getAutoUpdateInfo().getEmbedUrl()).isEqualTo("https://av.tib.eu/player/12345");
  }

  private List<BackendConfig.FieldProperties> getDefaultVocabParentsFieldProperties() {
    BackendConfig.FieldProperties fieldProperties = new BackendConfig.FieldProperties();
    fieldProperties.setFieldName("about");
    fieldProperties.setVocabIdentifier("hochschulfaechersystematik");
    fieldProperties.setVocabItemIdentifierField("id");
    fieldProperties.setAddMissingVocabParents(true);
    return List.of(fieldProperties);
  }

  @Test
  void testAddParentItemsForHierarchicalVocab() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de",
                    "about", List.of(
                            Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n009")
                    )
            )
            ), "id");
    when(vocabService.getParentMap("hochschulfaechersystematik")).thenReturn(Map.of(
            "https://w3id.org/kim/hochschulfaechersystematik/n009", "https://w3id.org/kim/hochschulfaechersystematik/n42",
            "https://w3id.org/kim/hochschulfaechersystematik/n42", "https://w3id.org/kim/hochschulfaechersystematik/n4"
    ));
    BackendConfig config = new BackendConfig();
    config.setFieldProperties(getDefaultVocabParentsFieldProperties());
    when(configService.getMetadataConfig()).thenReturn(config);

    metadataAutoUpdater.addMissingInfos(data);

    assertThat(data.get("about")).isInstanceOf(List.class);
    assertThat((List<?>) data.get("about")).hasSize(3);
  }

  @Test
  void testAboutWithMultipleParentSubjectsButAlsoWithChildSubjects() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de",
                    "about", List.of(
                            Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n4"),
                            Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n42"),
                            Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n6"),
                            Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n9")
                    )
            )
            ), "id");
    when(vocabService.getParentMap("hochschulfaechersystematik")).thenReturn(Map.of(
            "https://w3id.org/kim/hochschulfaechersystematik/n42", "https://w3id.org/kim/hochschulfaechersystematik/n4"
    ));
    BackendConfig config = new BackendConfig();
    config.setFieldProperties(getDefaultVocabParentsFieldProperties());
    when(configService.getMetadataConfig()).thenReturn(config);

    metadataAutoUpdater.addMissingInfos(data);

    assertThat(data.get("about")).isInstanceOf(List.class);
    assertThat((List<?>) data.get("about")).hasSize(4);
  }

}
