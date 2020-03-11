package eu.tib.oersi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.tib.oersi.domain.Author;
import eu.tib.oersi.domain.Didactics;
import eu.tib.oersi.domain.EducationalResource;
import eu.tib.oersi.domain.Institution;
import eu.tib.oersi.domain.Metadata;
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
public class MetadataServiceTest {

  @Autowired
  private MetadataService service;
  @MockBean
  private MetadataRepository repository;

  private Metadata newMetadata() {
    Metadata metadata = new Metadata();

    List<Author> authors = new ArrayList<>();
    Author author = new Author();
    author.setFamilyName("test");
    author.setGivenName("test");
    authors.add(author);
    metadata.setAuthors(authors);

    Didactics didactics = new Didactics();
    didactics.setAudience("testaudience");
    didactics.setEducationalUse("testeducationalUse");
    didactics.setInteractivityType("testinteractivityType");
    didactics.setTimeRequired("testtimeRequired");
    metadata.setDidactics(didactics);

    Institution institution = new Institution();
    institution.setName("name");
    institution.setRor("ror");
    metadata.setInstitution(institution);

    metadata.setSource("testsource");

    EducationalResource educationalResource = new EducationalResource();
    educationalResource.setDescription("test description");
    educationalResource.setInLanguage("DE");
    educationalResource.setKeywords(Arrays.asList("test1", "test2"));
    educationalResource.setLearningResourceType("testType");
    educationalResource.setLicense("CC0");
    educationalResource.setName("Test Title");
    educationalResource.setSubject("testsubject");
    educationalResource.setUrl("http://www.test.de");
    metadata.setEducationalResource(educationalResource);
    return metadata;
  }

  @Test
  public void testCreateOrUpdateWithoutExistingData() {
    Metadata metadata = newMetadata();
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  public void testCreateOrUpdateWithoutUrl() {
    Metadata metadata = newMetadata();
    metadata.getEducationalResource().setUrl(null);
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  public void testCreateOrUpdateWithExistingDataFoundById() {
    Metadata metadata = newMetadata();
    metadata.setId(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(metadata));
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  public void testCreateOrUpdateWithExistingDataFoundByUrl() {
    Metadata metadata = newMetadata();
    when(repository.findByEducationalResourceUrl(metadata.getEducationalResource().getUrl()))
        .thenReturn(Arrays
        .asList(
        (metadata)));
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  public void testDelete() {
    Metadata metadata = newMetadata();
    service.delete(metadata);
    verify(repository, times(1)).delete(metadata);
  }

  @Test
  public void testFindById() {
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
