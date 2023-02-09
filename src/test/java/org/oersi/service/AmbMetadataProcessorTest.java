package org.oersi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.domain.BackendConfig;
import org.oersi.domain.BackendMetadata;
import org.oersi.domain.OembedInfo;
import org.oersi.domain.VocabItem;
import org.oersi.repository.BackendConfigRepository;
import org.oersi.repository.VocabItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class AmbMetadataProcessorTest {

  private static final String TEST_IDENTIFIER = "test";

  @Autowired
  private AmbMetadataProcessor processor;

  @MockBean
  private LabelDefinitionService labelRepository;
  @MockBean
  private VocabItemRepository vocabItemRepository;
  @Autowired
  private BackendConfigRepository configRepository; // mock from ElasticsearchServicesMock

  @Test
  void testAddParentItemsForHierarchicalVocab() {
    BackendMetadata data = MetadataHelper.toMetadata(
      new HashMap<>(Map.of(
        "id", "https://www.test.de",
        "about", List.of(
          Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/n009")
        )
      )
    ));
    List<VocabItem> vocabItems = new ArrayList<>();
    VocabItem item1 = new VocabItem();
    item1.setParentKey("hochschulfaechersystematik");
    item1.setItemKey("https://w3id.org/kim/hochschulfaechersystematik/n009");
    item1.setParentKey("https://w3id.org/kim/hochschulfaechersystematik/n42");
    vocabItems.add(item1);
    VocabItem item2 = new VocabItem();
    item2.setParentKey("hochschulfaechersystematik");
    item2.setItemKey("https://w3id.org/kim/hochschulfaechersystematik/n42");
    item2.setParentKey("https://w3id.org/kim/hochschulfaechersystematik/n4");
    vocabItems.add(item2);
    when(vocabItemRepository.findByVocabIdentifier("hochschulfaechersystematik")).thenReturn(vocabItems);

    processor.setFeatureAddMissingParentItems(true);
    processor.process(data);

    assertThat(data.get("about")).isInstanceOf(List.class);
    assertThat((List<?>) data.get("about")).hasSize(3);
  }

  @Test
  void testUnsetLabelDefinition() {
    Map<String, Object> testData = new HashMap<>(Map.of("id", TEST_IDENTIFIER));
    when(labelRepository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(null);

    processor.addMissingLabels(testData);
    assertThat(testData).doesNotContainKey("prefLabel");
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
    when(labelRepository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    processor.addMissingLabels(testData);
    assertThat(testData.get("prefLabel")).isInstanceOf(Map.class);
    assertThat((Map<?, ?>) testData.get("prefLabel")).hasSize(3);

    testData = new HashMap<>(Map.of(
      "id", TEST_IDENTIFIER,
      "prefLabel", new HashMap<String, String>()
    ));
    when(labelRepository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    processor.addMissingLabels(testData);
    assertThat(testData.get("prefLabel")).isInstanceOf(Map.class);
    assertThat((Map<?, ?>) testData.get("prefLabel")).hasSize(3);
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
    when(labelRepository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    processor.addMissingLabels(testData);
    assertThat(testData.get("prefLabel")).isInstanceOf(Map.class);
    Map<String, String> prefLabel = MetadataHelper.parse(testData, "prefLabel", new TypeReference<>(){});
    assertThat(prefLabel)
      .hasSize(3)
      .containsEntry("de", "test4")
      .containsEntry("en", "test5")
      .containsEntry("fi", "test3");
  }

  @Test
  void testUpdateMetadata() {
    BackendMetadata data = MetadataHelper.toMetadata(
      new HashMap<>(Map.of(
        "id", "https://www.test.de",
        "about", List.of(new HashMap<>(Map.of("id", TEST_IDENTIFIER))),
        "audience", List.of(new HashMap<>(Map.of("id", TEST_IDENTIFIER))),
        "conditionsOfAccess", List.of(new HashMap<>(Map.of("id", TEST_IDENTIFIER))),
        "learningResourceType", List.of(new HashMap<>(Map.of("id", TEST_IDENTIFIER)))
      )));
    when(labelRepository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    processor.addMissingLabels(data);
    List.of("about", "audience", "conditionsOfAccess", "learningResourceType").forEach(field -> {
      List<Map<String, Object>> labelledConcept = MetadataHelper.parseList(data.getData(), field, new TypeReference<>(){});
      assertThat(labelledConcept).hasSize(1);
      Map<String, String> prefLabel = MetadataHelper.parse(labelledConcept.get(0), "prefLabel", new TypeReference<>(){});
      assertThat(prefLabel).isNotNull().hasSize(3);
    });
  }

  @Test
  void testUpdateMetadataWithoutLabelFields() {
    Map<String, Object> testData = new HashMap<>(Map.of("id", TEST_IDENTIFIER));
    when(labelRepository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    processor.addMissingLabels(testData);
    List.of("about", "audience", "conditionsOfAccess", "learningResourceType").forEach(field -> assertThat(testData.get(field)).isNull());
  }

  @Test
  void testOembedInfo() {
    BackendMetadata data = MetadataHelper.toMetadata(
      Map.of(
        "id", "https://www.test.de",
        "name", "test",
        "creator", List.of(
          Map.of(
            "type", "Person",
            "name", "GivenName FamilyName"
          ),
          Map.of(
            "type", "Organization",
            "name", "name",
            "id", "https://example.org/ror"
          )
        ),
        "image", "https://example.org/image/123.png",
        "encoding", List.of(new HashMap<>(Map.of(
          "type", "MediaObject",
          "embedUrl", "https://example.org/embed/#/123"
        )))
      ));
    OembedInfo oembedInfo = new OembedInfo();
    processor.processOembedInfo(oembedInfo, data);
    assertThat(oembedInfo.getTitle()).isEqualTo(data.getData().get("name"));
    assertThat(oembedInfo.getAuthors()).hasSize(2);
    assertThat(oembedInfo.getThumbnailUrl()).isEqualTo(data.getData().get("image"));
    assertThat(oembedInfo.getVideoEmbedUrl()).isEqualTo("https://example.org/embed/#/123");
  }

  @Test
  void testCreatorToPersonsMapping() {
    BackendMetadata data = MetadataHelper.toMetadata(
      new HashMap<>(Map.of(
        "id", "https://www.test.de",
        "name", "test",
        "creator", List.of(
          Map.of(
            "type", "Person",
            "name", "GivenName FamilyName"
          ),
          Map.of(
            "type", "Organization",
            "name", "name",
            "id", "https://example.org/ror"
          )
        )
      )));
    processor.process(data);
    assertThat(data.getAdditionalData()).isNotNull().containsEntry("persons", List.of(Map.of("type", "Person", "name", "GivenName FamilyName")));
  }
  @Test
  void testPublisherToInstitutionWhitelistMapping() {
    BackendConfig config = new BackendConfig();
    config.setCustomConfig(Map.of(
      "publisherToInstitutionWhitelist", List.of(
        Map.of("regex", ".*(ABC-institution).*"),
        Map.of("regex", ".*(XxxYyyZzz-institute of technology).*", "name", "XYZ-institute")
      )
    ));
    when(configRepository.findById("oersi_backend_config")).thenReturn(Optional.of(config));
    BackendMetadata data = MetadataHelper.toMetadata(
      new HashMap<>(Map.of(
        "id", "https://www.test.de",
        "name", "test",
        "publisher", List.of(
          Map.of(
            "type", "Organization",
            "name", "an organization that is not whitelisted"
          ),
          Map.of(
            "type", "Organization",
            "name", "an amazing ABC-institution (great)"
          ),
          Map.of(
            "type", "Organization",
            "name", "XxxYyyZzz-institute of technology"
          )
        )
      )));
    processor.process(data);
    assertThat(
      data.getAdditionalData()).isNotNull()
      .containsEntry(
        "institutions", List.of(
          Map.of("type", "Organization", "name", "ABC-institution"),
          Map.of("type", "Organization", "name", "XYZ-institute")
      )
    );
  }

}
