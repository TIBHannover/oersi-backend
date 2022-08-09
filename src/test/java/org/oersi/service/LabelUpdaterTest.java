package org.oersi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oersi.domain.*;
import org.oersi.repository.LabelDefinitionRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class LabelUpdaterTest {

  private static final String TEST_IDENTIFIER = "test";

  @MockBean
  private LabelDefinitionRepository repository;
  private LabelUpdater labelUpdater;
  @MockBean
  private JavaMailSender mailSender;

  @BeforeEach
  void cleanup() {
    labelUpdater = new LabelUpdater(repository);
  }

  private LocalizedString testData() {
    LocalizedString result = new LocalizedString();
    Map<String, String> localizedStrings = new HashMap<>();
    localizedStrings.put("de", "test4");
    localizedStrings.put("en", "test5");
    result.setLocalizedStrings(localizedStrings);
    return result;
  }

  private LabelDefinition testDefinition() {
    LabelDefinition definition = new LabelDefinition();
    definition.setIdentifier(TEST_IDENTIFIER);
    LocalizedString localizedString = new LocalizedString();
    Map<String, String> map = new HashMap<>();
    map.put("de", "test1");
    map.put("en", "test2");
    map.put("fi", "test3");
    localizedString.setLocalizedStrings(map);
    definition.setLabel(localizedString);
    return definition;
  }

  private Metadata newMetadata() {
    Metadata metadata = new Metadata();
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
  void testUnsetLabelDefinition() {
    LocalizedString testData = testData();

    when(repository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.empty());
    LocalizedString result = labelUpdater.addMissingLabels(TEST_IDENTIFIER, testData);
    assertThat(result).isSameAs(testData);

    LabelDefinition labelDefinition = new LabelDefinition();
    when(repository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(labelDefinition));
    result = labelUpdater.addMissingLabels(TEST_IDENTIFIER, testData);
    assertThat(result).isSameAs(testData);

    labelDefinition = new LabelDefinition();
    labelDefinition.setLabel(new LocalizedString());
    when(repository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(labelDefinition));
    result = labelUpdater.addMissingLabels(TEST_IDENTIFIER, testData);
    assertThat(result).isSameAs(testData);
  }

  @Test
  void testWithNonExistingData() {
    when(repository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(testDefinition()));
    LocalizedString result = labelUpdater.addMissingLabels(TEST_IDENTIFIER, null);
    assertThat(result).isNotNull();
    assertThat(result.getLocalizedStrings()).hasSize(3);

    when(repository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(testDefinition()));
    result = labelUpdater.addMissingLabels(TEST_IDENTIFIER, new LocalizedString());
    assertThat(result).isNotNull();
    assertThat(result.getLocalizedStrings()).hasSize(3);
  }

  @Test
  void testWithExistingData() {
    when(repository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(testDefinition()));
    LocalizedString result = labelUpdater.addMissingLabels(TEST_IDENTIFIER, testData());
    assertThat(result).isNotNull();
    assertThat(result.getLocalizedStrings()).hasSize(3);
    assertThat(result.getLocalizedStrings()).containsEntry("de", "test4");
    assertThat(result.getLocalizedStrings()).containsEntry("en", "test5");
    assertThat(result.getLocalizedStrings()).containsEntry("fi", "test3");
  }

  @Test
  void testUpdateMetadata() {
    when(repository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(testDefinition()));
    Metadata metadata = newMetadata();
    labelUpdater.addMissingLabels(metadata);
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
    when(repository.findByIdentifier(TEST_IDENTIFIER)).thenReturn(Optional.of(testDefinition()));
    Metadata metadata = new Metadata();
    labelUpdater.addMissingLabels(metadata);
    assertThat(metadata.getAbout()).isNull();
    assertThat(metadata.getAudience()).isNull();
    assertThat(metadata.getConditionsOfAccess()).isNull();
    assertThat(metadata.getLearningResourceType()).isNull();
  }

}
