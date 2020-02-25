package eu.tib.oersi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.tib.oersi.domain.Author;
import eu.tib.oersi.domain.Didactics;
import eu.tib.oersi.domain.Institution;
import eu.tib.oersi.domain.OerMetadata;
import eu.tib.oersi.domain.Work;
import eu.tib.oersi.repository.OerMetadataRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class OerMetadataServiceTest {

  @Autowired
  private OerMetadataService service;
  @MockBean
  private OerMetadataRepository repository;

  private OerMetadata newMetadata() {
    OerMetadata metadata = new OerMetadata();

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

    Work work = new Work();
    work.setDescription("test description");
    work.setInLanguage("DE");
    work.setKeywords(Arrays.asList("test1", "test2"));
    work.setLearningResourceType("testType");
    work.setLicense("CC0");
    work.setName("Test Title");
    work.setSubject("testsubject");
    work.setUrl("http://www.test.de");
    metadata.setWork(work);
    return metadata;
  }

  @Test
  public void testCreateOrUpdateWithoutExistingData() {
    OerMetadata metadata = newMetadata();
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  public void testCreateOrUpdateWithExistingDataFoundById() {
    OerMetadata metadata = newMetadata();
    metadata.setId(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(metadata));
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  public void testCreateOrUpdateWithExistingDataFoundByUrl() {
    OerMetadata metadata = newMetadata();
    when(repository.findByWorkUrl(metadata.getWork().getUrl())).thenReturn(Arrays.asList(
        (metadata)));
    service.createOrUpdate(metadata);
    verify(repository, times(1)).save(metadata);
  }

  @Test
  public void testDelete() {
    OerMetadata metadata = newMetadata();
    service.delete(metadata);
    verify(repository, times(1)).delete(metadata);
  }

  @Test
  public void testFindById() {
    OerMetadata metadata = newMetadata();
    when(repository.findById(1L)).thenReturn(Optional.of(metadata));
    OerMetadata result = service.findById(1L);
    assertThat(result).isNotNull();

    result = service.findById(null);
    assertThat(result).isNull();

    when(repository.findById(1L)).thenReturn(Optional.ofNullable(null));
    result = service.findById(1L);
    assertThat(result).isNull();
  }
}
