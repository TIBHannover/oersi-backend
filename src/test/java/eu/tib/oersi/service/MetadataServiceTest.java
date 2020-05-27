package eu.tib.oersi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import eu.tib.oersi.domain.About;
import eu.tib.oersi.domain.Audience;
import eu.tib.oersi.domain.Creator;
import eu.tib.oersi.domain.LearningResourceType;
import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.domain.MetadataDescription;
import eu.tib.oersi.repository.MetadataRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class MetadataServiceTest {

  @Autowired
  private MetadataService service;
  @MockBean
  private MetadataRepository repository;

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
    metadata.setAudience(audience);

    MetadataDescription metadataDescription = new MetadataDescription();
    metadataDescription.setIdentifier("http://example.url/desc/123");
    metadata.setMainEntityOfPage(metadataDescription);

    LearningResourceType learningResourceType = new LearningResourceType();
    learningResourceType.setIdentifier("testType");
    metadata.setLearningResourceType(learningResourceType);

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
  void testCreateOrUpdateWithoutMainEntityOfPage() {
    Metadata metadata = newMetadata();
    metadata.setMainEntityOfPage(null);
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithNoUrlMainEntityOfPageIdentifier() {
    Metadata metadata = newMetadata();
    metadata.getMainEntityOfPage().setIdentifier("TEST");
    service.createOrUpdate(metadata);
    metadata.getMainEntityOfPage().setIdentifier("!!$%");
    service.createOrUpdate(metadata);
    verify(repository, times(2)).save(metadata);
  }

  @Test
  void testCreateOrUpdateWithMainEntityOfPageSource() {
    Metadata metadata = newMetadata();
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
    assertThat(metadata.getMainEntityOfPage().getSource()).isEqualTo("example.url");

    metadata.getMainEntityOfPage().setIdentifier("http://www.example2.url/desc/123");
    service.createOrUpdate(metadata);
    assertThat(metadata.getMainEntityOfPage().getSource()).isEqualTo("example2.url");
  }

  @Test
  void testCreateOrUpdateWithMissingMainEntityOfPageIdentifier() {
    Metadata metadata = newMetadata();
    metadata.getMainEntityOfPage().setIdentifier(null);
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
    when(repository.findByIdentifier(metadata.getIdentifier()))
        .thenReturn(Arrays
        .asList(
        (metadata)));
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

    when(repository.findById(1L)).thenReturn(Optional.ofNullable(null));
    result = service.findById(1L);
    assertThat(result).isNull();
  }
}
