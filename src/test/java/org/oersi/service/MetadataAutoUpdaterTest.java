package org.oersi.service;

import org.junit.jupiter.api.Test;
import org.oersi.domain.About;
import org.oersi.domain.Audience;
import org.oersi.domain.ConditionsOfAccess;
import org.oersi.domain.LearningResourceType;
import org.oersi.domain.LocalizedString;
import org.oersi.domain.Media;
import org.oersi.domain.Metadata;
import org.oersi.domain.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class MetadataAutoUpdaterTest {

  private static final String TEST_IDENTIFIER = "test";

  @Autowired
  private MetadataAutoUpdater metadataAutoUpdater;
  @MockBean
  private LabelDefinitionService repository;

  private Metadata newMetadata() {
    Metadata metadata = new Metadata();
    metadata.setIdentifier("https://www.test.de");

    Audience audience = new Audience();
    audience.setIdentifier(TEST_IDENTIFIER);
    metadata.setAudience(new ArrayList<>(List.of(audience)));

    LearningResourceType learningResourceType = new LearningResourceType();
    learningResourceType.setIdentifier(TEST_IDENTIFIER);
    metadata.setLearningResourceType(new ArrayList<>(List.of(learningResourceType)));

    About about = new About();
    about.setIdentifier(TEST_IDENTIFIER);
    metadata.setAbout(new ArrayList<>(List.of(about)));

    ConditionsOfAccess coa = new ConditionsOfAccess();
    coa.setIdentifier(TEST_IDENTIFIER);
    metadata.setConditionsOfAccess(coa);

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

  private LocalizedString testData() {
    LocalizedString result = new LocalizedString();
    Map<String, String> localizedStrings = new HashMap<>();
    localizedStrings.put("de", "test4");
    localizedStrings.put("en", "test5");
    result.setLocalizedStrings(localizedStrings);
    return result;
  }
  private Map<String, String> testDefinition() {
    Map<String, String> map = new HashMap<>();
    map.put("de", "test1");
    map.put("en", "test2");
    map.put("fi", "test3");
    return map;
  }

  @Test
  void testUnsetLabelDefinition() {
    LocalizedString testData = testData();
    when(repository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(null);
    LocalizedString result = metadataAutoUpdater.addMissingLabels(TEST_IDENTIFIER, testData);
    assertThat(result).isSameAs(testData);
  }

  @Test
  void testWithNonExistingData() {
    when(repository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    LocalizedString result = metadataAutoUpdater.addMissingLabels(TEST_IDENTIFIER, null);
    assertThat(result).isNotNull();
    assertThat(result.getLocalizedStrings()).hasSize(3);

    when(repository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    result = metadataAutoUpdater.addMissingLabels(TEST_IDENTIFIER, new LocalizedString());
    assertThat(result).isNotNull();
    assertThat(result.getLocalizedStrings()).hasSize(3);
  }

  @Test
  void testWithExistingData() {
    when(repository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    LocalizedString result = metadataAutoUpdater.addMissingLabels(TEST_IDENTIFIER, testData());
    assertThat(result).isNotNull();
    assertThat(result.getLocalizedStrings()).hasSize(3);
    assertThat(result.getLocalizedStrings()).containsEntry("de", "test4");
    assertThat(result.getLocalizedStrings()).containsEntry("en", "test5");
    assertThat(result.getLocalizedStrings()).containsEntry("fi", "test3");
  }

  @Test
  void testUpdateMetadata() {
    when(repository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    Metadata metadata = newMetadata();
    metadataAutoUpdater.addMissingLabels(metadata);
    assertThat(metadata.getAbout().get(0).getPrefLabel()).isNotNull();
    assertThat(metadata.getAbout().get(0).getPrefLabel().getLocalizedStrings()).hasSize(3);
    assertThat(metadata.getAudience().get(0).getPrefLabel()).isNotNull();
    assertThat(metadata.getAudience().get(0).getPrefLabel().getLocalizedStrings()).hasSize(3);
    assertThat(metadata.getConditionsOfAccess().getPrefLabel()).isNotNull();
    assertThat(metadata.getConditionsOfAccess().getPrefLabel().getLocalizedStrings()).hasSize(3);
    assertThat(metadata.getLearningResourceType().get(0).getPrefLabel()).isNotNull();
    assertThat(metadata.getLearningResourceType().get(0).getPrefLabel().getLocalizedStrings()).hasSize(3);
  }

  @Test
  void testUpdateMetadataWithoutLabelFields() {
    when(repository.findLocalizedLabelByIdentifier(TEST_IDENTIFIER)).thenReturn(testDefinition());
    Metadata metadata = new Metadata();
    metadataAutoUpdater.addMissingLabels(metadata);
    assertThat(metadata.getAbout()).isNull();
    assertThat(metadata.getAudience()).isNull();
    assertThat(metadata.getConditionsOfAccess()).isNull();
    assertThat(metadata.getLearningResourceType()).isNull();
  }
}
