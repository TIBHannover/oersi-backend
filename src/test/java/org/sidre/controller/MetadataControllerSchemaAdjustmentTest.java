package org.sidre.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchContainerTest;
import org.sidre.domain.BackendMetadata;
import org.sidre.repository.MetadataRepository;
import org.sidre.service.MetadataFieldServiceImpl;
import org.sidre.service.PublicMetadataIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test of {@link MetadataController}.
 */
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "base-field-config.resourceIdentifier=url", "metadata.custom.processor=",
        "metadata.schema.location=classpath:schemas/alternative/schema.json",
        "base-field-config.metadataSource.field=info.source", "base-field-config.metadataSource.objectIdentifier=url",
        "base-field-config.metadataSource.useMultipleItems=false", "base-field-config.metadataSource.isObject=true",
        "base-field-config.metadataSource.queries[0].name=sourceName", "base-field-config.metadataSource.queries[0].field=name"
})
@WithMockUser(roles = {"MANAGE_METADATA"})
class MetadataControllerSchemaAdjustmentTest extends ElasticsearchContainerTest {

  /** base path of the {@link MetadataController} */
  private static final String METADATA_CONTROLLER_BASE_PATH = "/api/metadata";

  @Autowired
  private MockMvc mvc;
  @Autowired
  private MetadataRepository repository;
  @Autowired
  private PublicMetadataIndexService publicMetadataIndexService;
  @MockBean
  private JavaMailSender mailSender;


  private static String asJson(final Object obj) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(OffsetDateTime.class, new JsonSerializer<>() {
      @Override
      public void serialize(final OffsetDateTime offsetDateTime, final JsonGenerator jsonGenerator,
          final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime));
      }
    });
    objectMapper.registerModule(simpleModule);
    return objectMapper.writeValueAsString(obj);
  }

  private BackendMetadata createTestMetadata() {
    BackendMetadata data = getTestMetadata();
    BackendMetadata clone = MetadataFieldServiceImpl.toMetadata(data.getData(), "url");
    clone.setExtendedData(clone.getData());
    publicMetadataIndexService.updatePublicIndices(List.of(clone));
    return repository.save(data);
  }

  private BackendMetadata getTestMetadata() {
    return MetadataFieldServiceImpl.toMetadata(new HashMap<>(Map.ofEntries(
      Map.entry("url", "https://example.org"),
      Map.entry("name", "name"),
      Map.entry("description", "description"),
      Map.entry("info", Map.of("source",
        Map.of(
          "url", "https://example.org/desc/123",
          "name", "testname"
        )
      ))
    )), "url");
  }

  private Map<String, Object> getTestMetadataDto() {
    return getTestMetadata().getData();
  }

  @Test
  void testGetRequest() throws Exception {
    BackendMetadata metadata = createTestMetadata();

    mvc.perform(get(METADATA_CONTROLLER_BASE_PATH + "/" + metadata.getId()))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value(metadata.get("name")))
        .andExpect(jsonPath("$.url").value(metadata.get("url")));
  }

  @Test
  void testPostRequest() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
        .andExpect(content().json(
            "{\"description\":\"description\",\"info\":{\"source\":{\"name\":\"testname\",\"url\":\"https://example.org/desc/123\"}},\"name\":\"name\"}"));
  }

  @Test
  void testBulkUpdate() throws Exception {
    Map<String, Object> metadata1 = getTestMetadataDto();
    Map<String, Object> metadata2 = getTestMetadataDto();
    metadata2.put("url", "https://example.org/record2");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH + "/bulk").contentType(MediaType.APPLICATION_JSON)
        .content(asJson(List.of(metadata1, metadata2)))).andExpect(status().isOk())
      .andExpect(content().json("{\"success\":2,\"failed\":0,\"messages\":[]}"));
  }

  @Test
  void testBulkUpdateWithMissingRequiredParameter() throws Exception {
    Map<String, Object> metadata1 = getTestMetadataDto();
    Map<String, Object> metadata2 = getTestMetadataDto();
    metadata2.remove("url");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH + "/bulk").contentType(MediaType.APPLICATION_JSON)
        .content(asJson(List.of(metadata1, metadata2)))).andExpect(status().isBadRequest());
  }

  @Test
  void testPostRequestWithMissingRequiredParameter() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.remove("url");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isBadRequest());
  }

  @Test
  void testDeleteRequest() throws Exception {
    BackendMetadata existingMetadata = createTestMetadata();

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteAllRequest() throws Exception {
    createTestMetadata();

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH)
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteBySourceName() throws Exception {
    createTestMetadata();

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source")
      .contentType(MediaType.APPLICATION_JSON).content("{\"queryName\": \"sourceName\", \"queryParam\": \"testname\"}"))
      .andExpect(status().isOk());

    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteRequestWithNonExistingData() throws Exception {
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteSourceRequest() throws Exception {
    createTestMetadata();
    String encodedIdentifier = Base64.getUrlEncoder().encodeToString("https://example.org/desc/123".getBytes(StandardCharsets.UTF_8));
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source/" + encodedIdentifier))
      .andExpect(status().isOk());
    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteMainEntityOfPageForNonExistingDataRequest() throws Exception {
    String encodedIdentifier = Base64.getUrlEncoder().encodeToString("https://example.org/desc/123".getBytes(StandardCharsets.UTF_8));
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source/" + encodedIdentifier))
      .andExpect(status().isNotFound());
  }

  @Test
  void testDeleteMainEntityOfPageInvalidRequest() throws Exception {
    String encodedIdentifier = Base64.getUrlEncoder().encodeToString("invalid invalid".getBytes(StandardCharsets.UTF_8));
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source/" + encodedIdentifier))
      .andExpect(status().isBadRequest());
  }
}
