package org.oersi.service;

import org.junit.jupiter.api.Test;
import org.oersi.domain.*;
import org.oersi.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class MetadataServiceTest {

  @Autowired
  private MetadataService service;
  @MockBean
  private MetadataRepository repository;
  @MockBean
  private LabelService labelService;

  private Metadata newMetadata() {
    Metadata metadata = new Metadata();

    List<Creator> creators = new ArrayList<>();
    Creator author = new Creator();
    author.setType("Person");
    author.setName("test test");
    creators.add(author);

    Creator institution = new Creator();
    institution.setType("Organization");
    institution.setName("name");
    institution.setIdentifier("ror");
    creators.add(institution);

    metadata.setCreator(creators);

    Audience audience = new Audience();
    audience.setIdentifier("testaudience");
    metadata.setAudience(new ArrayList<>(List.of(audience)));

    MainEntityOfPage mainEntityOfPage = new MainEntityOfPage();
    mainEntityOfPage.setIdentifier("http://example.url/desc/123");
    metadata.setMainEntityOfPage(new ArrayList<>(List.of(mainEntityOfPage)));

    LearningResourceType learningResourceType = new LearningResourceType();
    learningResourceType.setIdentifier("testType");
    LocalizedString lrtPrefLabel = new LocalizedString();
    lrtPrefLabel.setLocalizedStrings(Map.of("de", "Kurs", "en", "course"));
    learningResourceType.setPrefLabel(lrtPrefLabel);
    metadata.setLearningResourceType(new ArrayList<>(List.of(learningResourceType)));

    List<About> subjects = new ArrayList<>();
    About about = new About();
    about.setIdentifier("testsubject");
    subjects.add(about);
    metadata.setAbout(subjects);

    metadata.setDescription("test description");
    metadata.setInLanguage("de");
    metadata.setLicense("CC0");
    metadata.setName("Test Title");
    metadata.setIdentifier("http://www.test.de");
    return metadata;
  }

  @Test
  void testCreateOrUpdateWithoutExistingData() {
    Metadata metadata = newMetadata();
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithMinimalData() {
    Metadata metadata = new Metadata();
    metadata.setName("Test Title");
    metadata.setIdentifier("http://www.test.de");
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithInvalidLanguageCodeInPrefLabel() {
    Metadata metadata = newMetadata();
    LocalizedString lrtPrefLabel = new LocalizedString();
    lrtPrefLabel.setLocalizedStrings(Map.of("invalid", "test"));
    metadata.getLearningResourceType().get(0).setPrefLabel(lrtPrefLabel);
    try {
      service.createOrUpdate(metadata);
      fail("Expect an exception as the prefLabel language code is invalid");
    } catch (IllegalArgumentException e) {
    }

    lrtPrefLabel = new LocalizedString();
    lrtPrefLabel.setLocalizedStrings(Map.of("zz", "test"));
    metadata.getLearningResourceType().get(0).setPrefLabel(lrtPrefLabel);
    try {
      service.createOrUpdate(metadata);
      fail("Expect an exception as the prefLabel language code is invalid");
    } catch (IllegalArgumentException e) {
    }
    verify(repository, times(0)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithEmptyMandatoryFields() {
    Metadata metadata = newMetadata();
    metadata.setIdentifier("");
    metadata.setName("");
    try {
      service.createOrUpdate(metadata);
      fail("Expect an exception as there are empty mandatory fields");
    } catch (IllegalArgumentException e) {
    }
    verify(repository, times(0)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithIncompleteLabel() {
    Metadata metadata = newMetadata();
    LocalizedString lrtPrefLabel = new LocalizedString();
    metadata.getLearningResourceType().get(0).setPrefLabel(lrtPrefLabel);
    metadata.getAudience().get(0).setIdentifier(null);
    service.createOrUpdate(metadata);
    verify(labelService, times(0)).createOrUpdate(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testCreateOrUpdateWithoutMainEntityOfPage() {
    Metadata metadata = newMetadata();
    metadata.setMainEntityOfPage(null);
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithNoUrlMainEntityOfPageIdentifier() {
    Metadata metadata = newMetadata();
    metadata.getMainEntityOfPage().get(0).setIdentifier("TEST");
    service.createOrUpdate(metadata);
    metadata.getMainEntityOfPage().get(0).setIdentifier("!!$%");
    service.createOrUpdate(metadata);
    verify(repository, times(2)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithMainEntityOfPageSource() {
    Metadata metadata = newMetadata();
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
    assertThat(metadata.getMainEntityOfPage().get(0).getProvider()).isNotNull();
    assertThat(metadata.getMainEntityOfPage().get(0).getProvider().getName())
        .isEqualTo("example.url");

    metadata.getMainEntityOfPage().get(0).setIdentifier("http://www.example2.url/desc/123");
    metadata.getMainEntityOfPage().get(0).getProvider().setName(null);
    service.createOrUpdate(metadata);
    assertThat(metadata.getMainEntityOfPage().get(0).getProvider()).isNotNull();
    assertThat(metadata.getMainEntityOfPage().get(0).getProvider().getName())
        .isEqualTo("example2.url");
  }

  @Test
  void testUpdateMainEntityOfPagesNotSet() {
    Metadata existingData = newMetadata();
    existingData.setMainEntityOfPage(null);
    when(repository.findByIdentifier(existingData.getIdentifier()))
        .thenReturn(List.of(existingData));

    Metadata metadata = newMetadata();
    metadata.getMainEntityOfPage().get(0).setIdentifier("http://www.example2.url/desc/123");
    service.createOrUpdate(metadata);
    assertEquals(1, metadata.getMainEntityOfPage().size());
  }

  @Test
  void testUpdateMainEntityOfPagesDisjunct() {
    Metadata existingData = newMetadata();
    when(repository.findByIdentifier(existingData.getIdentifier()))
        .thenReturn(List.of(existingData));

    Metadata metadata = newMetadata();
    metadata.getMainEntityOfPage().get(0).setIdentifier("http://www.example2.url/desc/123");
    service.createOrUpdate(metadata);
    assertEquals(2, metadata.getMainEntityOfPage().size());
  }

  @Test
  void testUpdateMainEntityOfPagesMerge() {
    Metadata existingData = newMetadata();
    when(repository.findByIdentifier(existingData.getIdentifier()))
        .thenReturn(List.of(existingData));

    Metadata metadata = newMetadata();
    MainEntityOfPage mainEntityOfPage = new MainEntityOfPage();
    mainEntityOfPage.setIdentifier("http://www.example2.url/desc/123");
    metadata.getMainEntityOfPage().add(mainEntityOfPage);
    service.createOrUpdate(metadata);
    assertEquals(2, metadata.getMainEntityOfPage().size());
  }

  @Test
  void testCreateOrUpdateWithGivenProvider() {
    Metadata metadata = newMetadata();
    Provider provider = new Provider();
    provider.setName("testname");
    metadata.getMainEntityOfPage().get(0).setProvider(provider);
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
    assertThat(metadata.getMainEntityOfPage().get(0).getProvider()).isNotNull();
    assertThat(metadata.getMainEntityOfPage().get(0).getProvider().getName()).isEqualTo("testname");
  }

  @Test
  void testCreateOrUpdateWithMissingMainEntityOfPageIdentifier() {
    Metadata metadata = newMetadata();
    metadata.getMainEntityOfPage().get(0).setIdentifier(null);
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithoutUrl() {
    Metadata metadata = newMetadata();
    metadata.setIdentifier(null);
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithExistingDataFoundById() {
    Metadata metadata = newMetadata();
    metadata.setId(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(metadata));
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithExistingDataFoundByUrl() {
    Metadata metadata = newMetadata();
    when(repository.findByIdentifier(metadata.getIdentifier())).thenReturn(List.of(metadata));
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  void testDelete() {
    Metadata metadata = newMetadata();
    service.delete(metadata);
    verify(repository, times(1)).delete(metadata);
  }

  @Test
  void testFindById() {
    Metadata metadata = newMetadata();
    when(repository.findById(1L)).thenReturn(Optional.of(metadata));
    Metadata result = service.findById(1L);
    assertThat(result).isNotNull();

    result = service.findById(null);
    assertThat(result).isNull();

    when(repository.findById(1L)).thenReturn(Optional.empty());
    result = service.findById(1L);
    assertThat(result).isNull();
  }
}
