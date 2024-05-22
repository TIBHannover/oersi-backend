package org.oersi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.domain.BackendMetadata;
import org.oersi.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class MetadataServiceTest {

  @Autowired
  private MetadataService service;
  @Autowired
  private MetadataRepository repository; // mock from ElasticsearchServicesMock
  @MockBean
  private ConfigService configService;
  @MockBean
  private LabelService labelService;
  @MockBean
  private JavaMailSender mailSender;

  @BeforeEach
  public void setup() {
  }

  private BackendMetadata newMetadata() {
    return MetadataFieldServiceImpl.toMetadata(new HashMap<>(Map.ofEntries(
      Map.entry("@context", List.of("https://w3id.org/kim/amb/context.jsonld", Map.of("@language", "de"))),
      Map.entry("id", "https://www.test.de"),
      Map.entry("name", "Test Title"),
      Map.entry("description", "test description"),
      Map.entry("inLanguage", new ArrayList<>(List.of("de"))),
      Map.entry("license", Map.of("id", "https://creativecommons.org/publicdomain/zero/1.0/")),
      Map.entry("creator", new ArrayList<>(List.of(
        Map.of(
          "type", "Person",
          "name", "test test"
        ),
        Map.of(
          "type", "Organization",
          "name", "name",
          "id", "https://example.org/ror"
        )
      ))),
      Map.entry("audience", new ArrayList<>(List.of(
        new HashMap<>(Map.of("id", "http://purl.org/dcx/lrmi-vocabs/educationalAudienceRole/testaudience"))
      ))),
      Map.entry("learningResourceType", new ArrayList<>(List.of(
        new HashMap<>(Map.of(
          "id", "https://w3id.org/kim/hcrt/testType",
          "prefLabel", Map.of("de", "Kurs", "en", "course")
        ))
      ))),
      Map.entry("about", new ArrayList<>(List.of(
        new HashMap<>(Map.of("id", "https://w3id.org/kim/hochschulfaechersystematik/testsubject"))
      ))),
      Map.entry("mainEntityOfPage", new ArrayList<>(List.of(
        Map.of(
          "id", "http://example.url/desc/123",
          "provider", Map.of("id", "http://example.url/provider/testprovider", "name", "provider name")
        )
      )))
    )), "id");
  }

  private Map<String, Object> buildMainEntityOfPage(String id, String provider) {
    return Map.of(
      "id", id,
      "provider", Map.of("id", "http://example.url/provider/" + provider, "name", provider)
    );
  }

  @Test
  void testCreateOrUpdateWithoutExistingData() {
    BackendMetadata metadata = newMetadata();
    service.createOrUpdate(metadata);
    verify(repository, times(1)).saveAll(anyList());
  }

  @Test
  void testCreateOrUpdateWithMinimalData() {
    BackendMetadata dummy = newMetadata();
    BackendMetadata metadata = MetadataFieldServiceImpl.toMetadata(new HashMap<>(Map.of(
      "@context", dummy.getData().get("@context"),
      "id", dummy.getData().get("id"),
      "name", dummy.getData().get("name"),
      "mainEntityOfPage", dummy.getData().get("mainEntityOfPage")
    )), "id");
    service.createOrUpdate(metadata);
    verify(repository, times(1)).saveAll(anyList());
  }

  @Test
  void testCreateOrUpdateWithInvalidLanguageCodeInPrefLabel() {
    BackendMetadata metadata = newMetadata();
    metadata.getData().put(
      "learningResourceType", new ArrayList<>(List.of(
        new HashMap<>(Map.of(
          "id", "https://w3id.org/kim/hcrt/testType",
          "prefLabel", Map.of("invalid", "test")
        ))
      ))
    );
    MetadataService.MetadataUpdateResult result = service.createOrUpdate(metadata);
    assertThat(result.getSuccess()).isFalse();

    metadata.getData().put(
      "learningResourceType", new ArrayList<>(List.of(
        new HashMap<>(Map.of(
          "id", "https://w3id.org/kim/hcrt/testType",
          "prefLabel", Map.of("zz", "test")
        ))
      ))
    );
    result = service.createOrUpdate(metadata);
    assertThat(result.getSuccess()).isFalse();
    verify(repository, times(0)).saveAll(anyList());
  }

  @Test
  void testCreateOrUpdateWithEmptyMandatoryFields() {
    BackendMetadata metadata = newMetadata();
    metadata.getData().put("id", "");
    metadata.getData().put("name", "");
    MetadataService.MetadataUpdateResult result = service.createOrUpdate(metadata);
    assertThat(result.getSuccess()).isFalse();
    verify(repository, times(0)).saveAll(anyList());
  }

  @Test
  void testCreateOrUpdateWithDurationAndCaption() {
    BackendMetadata metadata = newMetadata();
    metadata.getData().put("caption", List.of(Map.of(
      "id", "https://example.org/subs-de.vtt",
      "type", "MediaObject",
      "encodingFormat", "text/vtt",
      "inLanguage", "de"
    )));
    metadata.getData().put("duration", "PT47M58S");
    MetadataService.MetadataUpdateResult result = service.createOrUpdate(metadata);
    assertThat(result.getSuccess()).isTrue();
    verify(repository, times(1)).saveAll(anyList());
  }
  @Test
  void testCreateOrUpdateWithInvalidDuration() {
    BackendMetadata metadata = newMetadata();
    metadata.getData().put("duration", "invalid");
    MetadataService.MetadataUpdateResult result = service.createOrUpdate(metadata);
    assertThat(result.getSuccess()).isFalse();
    verify(repository, times(0)).saveAll(anyList());
  }

  @Test
  void testCreateOrUpdateWithoutMainEntityOfPage() {
    BackendMetadata metadata = newMetadata();
    metadata.getData().remove("mainEntityOfPage");
    service.createOrUpdate(metadata);
    verify(repository, times(0)).saveAll(anyList());
  }

  @Test
  void testCreateOrUpdateWithNoUrlMainEntityOfPageIdentifier() {
    BackendMetadata metadata = newMetadata();
    metadata.getData().put("mainEntityOfPage", new ArrayList<>(List.of(buildMainEntityOfPage("TEST", "testprovider"))));
    service.createOrUpdate(metadata);
    metadata.getData().put("mainEntityOfPage", new ArrayList<>(List.of(buildMainEntityOfPage("!!$%", "testprovider2"))));
    service.createOrUpdate(metadata);
    verify(repository, times(0)).saveAll(anyList());
  }

  @Test
  void testUpdateMainEntityOfPagesNotSet() {
    BackendMetadata existingData = newMetadata();
    existingData.getData().remove("mainEntityOfPage");
    when(repository.findById(existingData.getId())).thenReturn(Optional.of(existingData));

    BackendMetadata metadata = newMetadata();
    metadata.getData().put("mainEntityOfPage", new ArrayList<>(List.of(buildMainEntityOfPage("http://example2.url/desc/123", "testprovider2"))));
    BackendMetadata result = service.createOrUpdate(metadata).getMetadata();
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(result.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    assertEquals(1, mainEntityOfPage.size());
  }

  @Test
  void testUpdateMainEntityOfPagesDisjunct() {
    BackendMetadata existingData = newMetadata();
    when(repository.findById(existingData.getId())).thenReturn(Optional.of(existingData));

    BackendMetadata metadata = newMetadata();
    metadata.getData().put("mainEntityOfPage", new ArrayList<>(List.of(buildMainEntityOfPage("http://example2.url/desc/123", "testprovider2"))));
    BackendMetadata result = service.createOrUpdate(metadata).getMetadata();
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(result.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    assertEquals(2, mainEntityOfPage.size());
    List<Map<String, Object>> mainEntityOfPageInternal = MetadataHelper.parseList(result.getAdditionalData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPageInternal);
    assertEquals(2, mainEntityOfPageInternal.size());
  }

  @Test
  void testUpdateMainEntityOfPagesMerge() {
    BackendMetadata existingData = newMetadata();
    when(repository.findById(existingData.getId())).thenReturn(Optional.of(existingData));

    BackendMetadata metadata = newMetadata();
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(metadata.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    mainEntityOfPage.add(buildMainEntityOfPage("http://example2.url/desc/123", "testprovider2"));
    metadata.getData().put("mainEntityOfPage", mainEntityOfPage);
    BackendMetadata result = service.createOrUpdate(metadata).getMetadata();
    mainEntityOfPage = MetadataHelper.parseList(result.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    assertEquals(2, mainEntityOfPage.size());
  }

  @Test
  void testCreateOrUpdateWithGivenProvider() {
    BackendMetadata metadata = newMetadata();
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(metadata.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    mainEntityOfPage.get(0).put("provider", Map.of("name", "testname", "id", "https://example.org/provider/provider"));
    metadata.getData().put("mainEntityOfPage", mainEntityOfPage);
    BackendMetadata result = service.createOrUpdate(metadata).getMetadata();
    verify(repository, times(1)).saveAll(anyList());
    mainEntityOfPage = MetadataHelper.parseList(result.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    Map<String, Object> provider = MetadataHelper.parse(mainEntityOfPage.get(0), "provider", new TypeReference<>() {});
    assertThat(provider).isNotNull().containsEntry("name", "testname");
  }

  @Test
  void testCreateOrUpdateWithMissingMainEntityOfPageIdentifier() {
    BackendMetadata metadata = newMetadata();
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(metadata.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    mainEntityOfPage.get(0).remove("id");
    service.createOrUpdate(metadata);
    verify(repository, times(1)).saveAll(anyList());
  }

  @Test
  void testCreateOrUpdateWithExistingDataFoundById() {
    BackendMetadata metadata = newMetadata();
    when(repository.findById(metadata.getId())).thenReturn(Optional.of(metadata));
    service.createOrUpdate(metadata);
    verify(repository, times(1)).saveAll(anyList());
  }

  @Test
  void testDelete() {
    BackendMetadata metadata = newMetadata();
    service.delete(metadata, false);
    verify(repository, times(1)).deleteAll(anyList());
  }

  @Test
  void testFindById() {
    BackendMetadata metadata = newMetadata();
    when(repository.findById(metadata.getId())).thenReturn(Optional.of(metadata));
    BackendMetadata result = service.findById(metadata.getId());
    assertThat(result).isNotNull();

    result = service.findById(null);
    assertThat(result).isNull();

    when(repository.findById(metadata.getId())).thenReturn(Optional.empty());
    result = service.findById(metadata.getId());
    assertThat(result).isNull();
  }

}
