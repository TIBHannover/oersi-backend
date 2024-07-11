package org.sidre.service;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class MetadataAutoUpdaterTest {

  private static final String TEST_IDENTIFIER = "test";

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
  void testAddFlatParentItemsForHierarchicalVocab() {
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de",
                    "about", List.of("https://w3id.org/kim/hochschulfaechersystematik/n009")
            )
            ), "id");
    when(vocabService.getParentMap("hochschulfaechersystematik")).thenReturn(Map.of(
            "https://w3id.org/kim/hochschulfaechersystematik/n009", "https://w3id.org/kim/hochschulfaechersystematik/n42",
            "https://w3id.org/kim/hochschulfaechersystematik/n42", "https://w3id.org/kim/hochschulfaechersystematik/n4"
    ));
    BackendConfig config = new BackendConfig();
    List<BackendConfig.FieldProperties> defaultVocabParentsFieldProperties = getDefaultVocabParentsFieldProperties();
    defaultVocabParentsFieldProperties.get(0).setVocabItemIdentifierField(null);
    config.setFieldProperties(defaultVocabParentsFieldProperties);
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

  private BackendConfig.FieldProperties getDefaultVocabLabelProperties(String fieldName) {
    BackendConfig.FieldProperties fieldProperties = new BackendConfig.FieldProperties();
    fieldProperties.setFieldName(fieldName);
    fieldProperties.setVocabItemIdentifierField("id");
    fieldProperties.setVocabItemLabelField("prefLabel");
    fieldProperties.setAddMissingVocabLabels(true);
    return fieldProperties;
  }
  private List<BackendConfig.FieldProperties> getDefaultVocabLabelProperties() {
    return List.of(getDefaultVocabLabelProperties("about"));
  }

  private BackendMetadata getVocabLabelTestData(Map<String, Object> testData) {
    return MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de",
                    "about", List.of(testData)
            )
            ), "id");
  }

  @Test
  void testUnsetLabelDefinition() {
    Map<String, Object> testData = new HashMap<>(Map.of("id", TEST_IDENTIFIER));
    when(vocabService.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(null);
    BackendConfig config = new BackendConfig();
    config.setFieldProperties(getDefaultVocabLabelProperties());
    when(configService.getMetadataConfig()).thenReturn(config);

    BackendMetadata data = getVocabLabelTestData(testData);
    metadataAutoUpdater.addMissingLabels(data);
    List<Map<String, Object>> labelledConcept = MetadataHelper.parseList(data.getData(), "about", new TypeReference<>(){});
    assertThat(labelledConcept).hasSize(1);
    assertThat(labelledConcept.get(0)).doesNotContainKey("prefLabel");
  }

  private Map<String, String> testDefinition() {
    Map<String, String> map = new HashMap<>();
    map.put("de", "test1");
    map.put("en", "test2");
    map.put("fi", "test3");
    return map;
  }

  @Test
  void testWithNonExistingData() {
    Map<String, Object> testData = new HashMap<>(Map.of(
            "id", TEST_IDENTIFIER
    ));
    when(vocabService.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    BackendConfig config = new BackendConfig();
    config.setFieldProperties(getDefaultVocabLabelProperties());
    when(configService.getMetadataConfig()).thenReturn(config);
    BackendMetadata data = getVocabLabelTestData(testData);
    metadataAutoUpdater.addMissingLabels(data);
    List<Map<String, Object>> labelledConcept = MetadataHelper.parseList(data.getData(), "about", new TypeReference<>(){});
    assertThat(labelledConcept).hasSize(1);
    Map<String, String> prefLabel = MetadataHelper.parse(labelledConcept.get(0), "prefLabel", new TypeReference<>(){});
    assertThat(prefLabel).hasSize(3);

    testData = new HashMap<>(Map.of(
            "id", TEST_IDENTIFIER,
            "prefLabel", new HashMap<String, String>()
    ));
    when(vocabService.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    data = getVocabLabelTestData(testData);
    metadataAutoUpdater.addMissingLabels(data);
    labelledConcept = MetadataHelper.parseList(data.getData(), "about", new TypeReference<>(){});
    assertThat(labelledConcept).hasSize(1);
    prefLabel = MetadataHelper.parse(labelledConcept.get(0), "prefLabel", new TypeReference<>(){});
    assertThat(prefLabel).hasSize(3);
  }

  @Test
  void testWithExistingData() {
    Map<String, Object> testData = new HashMap<>(Map.of(
            "id", TEST_IDENTIFIER,
            "prefLabel", new HashMap<>(Map.of(
                    "de", "test4",
                    "en", "test5"
            ))
    ));
    when(vocabService.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    BackendConfig config = new BackendConfig();
    config.setFieldProperties(getDefaultVocabLabelProperties());
    when(configService.getMetadataConfig()).thenReturn(config);
    BackendMetadata data = getVocabLabelTestData(testData);
    metadataAutoUpdater.addMissingLabels(data);
    List<Map<String, Object>> labelledConcept = MetadataHelper.parseList(data.getData(), "about", new TypeReference<>(){});
    assertThat(labelledConcept).hasSize(1);
    Map<String, String> prefLabel = MetadataHelper.parse(labelledConcept.get(0), "prefLabel", new TypeReference<>(){});
    assertThat(prefLabel)
            .hasSize(3)
            .containsEntry("de", "test4")
            .containsEntry("en", "test5")
            .containsEntry("fi", "test3");
  }

  @Test
  void testUpdateMetadata() {
    BackendConfig config = new BackendConfig();
    List<BackendConfig.FieldProperties> defaultVocabParentsFieldProperties = getDefaultVocabParentsFieldProperties();
    defaultVocabParentsFieldProperties.get(0).setVocabItemIdentifierField(null);
    config.setFieldProperties(Stream.of("about", "audience", "conditionsOfAccess", "learningResourceType").map(this::getDefaultVocabLabelProperties).toList());
    when(configService.getMetadataConfig()).thenReturn(config);

    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de",
                    "about", List.of(new HashMap<>(Map.of("id", TEST_IDENTIFIER))),
                    "audience", List.of(new HashMap<>(Map.of("id", TEST_IDENTIFIER))),
                    "conditionsOfAccess", List.of(new HashMap<>(Map.of("id", TEST_IDENTIFIER))),
                    "learningResourceType", List.of(new HashMap<>(Map.of("id", TEST_IDENTIFIER)))
            )), "id");
    when(vocabService.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    metadataAutoUpdater.addMissingLabels(data);
    List.of("about", "audience", "conditionsOfAccess", "learningResourceType").forEach(field -> {
      List<Map<String, Object>> labelledConcept = MetadataHelper.parseList(data.getData(), field, new TypeReference<>(){});
      assertThat(labelledConcept).hasSize(1);
      Map<String, String> prefLabel = MetadataHelper.parse(labelledConcept.get(0), "prefLabel", new TypeReference<>(){});
      assertThat(prefLabel).isNotNull().hasSize(3);
    });
  }

  @Test
  void testUpdateMetadataWithoutLabelFields() {
    when(vocabService.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    BackendConfig config = new BackendConfig();
    List<BackendConfig.FieldProperties> defaultVocabParentsFieldProperties = getDefaultVocabParentsFieldProperties();
    defaultVocabParentsFieldProperties.get(0).setVocabItemIdentifierField(null);
    config.setFieldProperties(Stream.of("about", "audience", "conditionsOfAccess", "learningResourceType").map(this::getDefaultVocabLabelProperties).toList());
    when(configService.getMetadataConfig()).thenReturn(config);
    BackendMetadata data = MetadataFieldServiceImpl.toMetadata(
            new HashMap<>(Map.of(
                    "id", "https://www.test.de"
            )
            ), "id");
    metadataAutoUpdater.addMissingLabels(data);
    List.of("about", "audience", "conditionsOfAccess", "learningResourceType").forEach(field -> assertThat(data.get(field)).isNull());
  }

}
