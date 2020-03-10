package eu.tib.oersi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tib.oersi.domain.Author;
import eu.tib.oersi.domain.Didactics;
import eu.tib.oersi.domain.EducationalResource;
import eu.tib.oersi.domain.Institution;
import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.dto.MetadataDto;
import eu.tib.oersi.repository.MetadataRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test of {@link MetadataController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = {"MANAGE_OERMETADATA"})
public class MetadataControllerTest {

  /** base path of the {@link MetadataController} */
  private static final String METADATA_CONTROLLER_BASE_PATH = "/api/metadata";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private MetadataRepository repository;

  @Autowired
  private ModelMapper modelMapper;

  @AfterEach
  public void cleanup() {
    repository.deleteAll();
    repository.flush();
  }

  public static String asJson(final Object obj) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(obj);
  }

  private Metadata createTestMetadata() {
    return repository.saveAndFlush(getTestMetadata());
  }

  private Metadata getTestMetadata() {
    Metadata metadata = new Metadata();
    Author author = new Author();
    author.setFamilyName("FamilyName");
    author.setGivenName("GivenName");
    metadata.setAuthors(Arrays.asList(author));
    Didactics didactics = new Didactics();
    didactics.setAudience("audience");
    didactics.setEducationalUse("educationalUse");
    didactics.setInteractivityType("interactivityType");
    didactics.setTimeRequired("timeRequired");
    metadata.setDidactics(didactics);
    EducationalResource educationalResource = new EducationalResource();
    educationalResource.setDescription("description");
    educationalResource.setName("name");
    educationalResource.setSubject("subject");
    educationalResource.setLicense("license");
    educationalResource.setUrl("http://example.url");
    educationalResource.setInLanguage("inLanguage");
    educationalResource.setLearningResourceType("learningResourceType");
    metadata.setEducationalResource(educationalResource);
    Institution institution = new Institution();
    institution.setName("name");
    metadata.setInstitution(institution);
    metadata.setSource("TEST");
    metadata.setDateModifiedInternal(LocalDateTime.now());
    return metadata;
  }

  private MetadataDto getTestMetadataDto() {
    return modelMapper.map(getTestMetadata(), MetadataDto.class);
  }

  @Test
  public void testGetRequest() throws Exception {
    Metadata metadata = createTestMetadata();

    mvc.perform(get(METADATA_CONTROLLER_BASE_PATH + "/" + metadata.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.educationalResource.name").value(metadata.getEducationalResource()
            .getName()))
        .andExpect(jsonPath("$.educationalResource.url").value(metadata.getEducationalResource()
            .getUrl()));
  }

  @Test
  public void testGetRequestWithNonExistingData() throws Exception {
    mvc.perform(get(METADATA_CONTROLLER_BASE_PATH + "/1000"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testPostRequest() throws Exception {
    MetadataDto metadata = getTestMetadataDto();

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata)))
        .andExpect(status().isOk())
        .andExpect(content().json(
            "{\"authors\":[{\"givenName\":\"GivenName\",\"familyName\":\"FamilyName\"}],\"didactics\":{\"audience\":\"audience\",\"educationalUse\":\"educationalUse\",\"interactivityType\":\"interactivityType\",\"timeRequired\":\"timeRequired\"},\"educationalResource\":{\"description\":\"description\",\"inLanguage\":\"inLanguage\",\"learningResourceType\":\"learningResourceType\",\"license\":\"license\",\"name\":\"name\",\"subject\":\"subject\",\"url\":\"http://example.url\"},\"institution\":{\"name\":\"name\"},\"source\":\"TEST\"}"));
  }

  @Test
  public void testPostRequestWithMissingRequiredParameter() throws Exception {
    MetadataDto metadata = getTestMetadataDto();
    metadata.setEducationalResource(null);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testPutRequest() throws Exception {
    Metadata existingMetadata = createTestMetadata();
    MetadataDto metadata = getTestMetadataDto();
    metadata.setSource("TEST2");
    metadata.setId(existingMetadata.getId());

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata)))
        .andExpect(status().isOk())
        .andExpect(content().json(
            "{\"authors\":[{\"givenName\":\"GivenName\",\"familyName\":\"FamilyName\"}],\"didactics\":{\"audience\":\"audience\",\"educationalUse\":\"educationalUse\",\"interactivityType\":\"interactivityType\",\"timeRequired\":\"timeRequired\"},\"educationalResource\":{\"description\":\"description\",\"inLanguage\":\"inLanguage\",\"learningResourceType\":\"learningResourceType\",\"license\":\"license\",\"name\":\"name\",\"subject\":\"subject\",\"url\":\"http://example.url\"},\"institution\":{\"name\":\"name\"},\"source\":\"TEST2\"}"));

    Assert.assertEquals(1, repository.count());
  }

  @Test
  public void testPutRequestWithMissingRequiredParameter() throws Exception {
    Metadata existingMetadata = createTestMetadata();
    MetadataDto metadata = getTestMetadataDto();
    metadata.setSource("TEST2");
    metadata.setId(existingMetadata.getId());
    metadata.setEducationalResource(null);

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testPutRequestWithIdMismatch() throws Exception {
    Metadata existingMetadata = createTestMetadata();
    MetadataDto metadata = getTestMetadataDto();
    metadata.setId(1000L);
    metadata.setSource("TEST2");

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata)))
        .andExpect(status().isBadRequest());

    metadata.setId(null);
    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testPutRequestWithNonExistingData() throws Exception {
    MetadataDto metadata = getTestMetadataDto();
    metadata.setSource("TEST2");
    metadata.setId(1L);

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testDeleteRequest() throws Exception {
    Metadata existingMetadata = createTestMetadata();

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    Assert.assertEquals(0, repository.count());
  }

  @Test
  public void testDeleteRequestWithNonExistingData() throws Exception {
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/1")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    Assert.assertEquals(0, repository.count());
  }
}
