package org.sidre.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchContainerTest;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.VocabItem;
import org.sidre.repository.BackendConfigRepository;
import org.sidre.repository.MetadataRepository;
import org.sidre.repository.VocabItemRepository;
import org.sidre.service.MetadataFieldServiceImpl;
import org.sidre.service.MetadataHelper;
import org.sidre.service.PublicMetadataIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.PutIndexTemplateRequest;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test of {@link MetadataController}.
 */
@AutoConfigureMockMvc
@WithMockUser(roles = {"MANAGE_METADATA"})
class MetadataControllerTest extends ElasticsearchContainerTest {

  /** base path of the {@link MetadataController} */
  private static final String METADATA_CONTROLLER_BASE_PATH = "/api/metadata";

  @Autowired
  private MockMvc mvc;
  @Autowired
  private MetadataRepository repository;
  @Autowired
  private BackendConfigRepository configRepository;
  @Autowired
  private VocabItemRepository vocabItemRepository;
  @Autowired
  private ElasticsearchOperations elasticsearchOperations;
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

  private BackendConfig setupPublicIndices() {
    BackendConfig initialConfig = new BackendConfig();
    initialConfig.setMetadataIndexName("oer_data_123");
    initialConfig.setExtendedMetadataIndexName("oer_data_extended_123");
    configRepository.save(initialConfig);
    Document mapping = Document.parse("{\"dynamic\": \"false\"}");
    IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(initialConfig.getMetadataIndexName()));
    var request = PutIndexTemplateRequest.builder().withName(initialConfig.getMetadataIndexName()).withIndexPatterns(initialConfig.getMetadataIndexName()).withMapping(mapping).build();
    indexOperations.putIndexTemplate(request);
    IndexOperations extendedIndexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(initialConfig.getExtendedMetadataIndexName()));
    request = PutIndexTemplateRequest.builder().withName(initialConfig.getExtendedMetadataIndexName()).withIndexPatterns(initialConfig.getExtendedMetadataIndexName()).withMapping(mapping).build();
    extendedIndexOperations.putIndexTemplate(request);
    return initialConfig;
  }

  private BackendMetadata createTestMetadata() {
    BackendMetadata data = getTestMetadata();
    BackendMetadata clone = MetadataFieldServiceImpl.toMetadata(data.getData(), "id");
    clone.setExtendedData(clone.getData());
    publicMetadataIndexService.updatePublicIndices(List.of(clone));
    return repository.save(data);
  }

  private BackendMetadata getTestMetadata() {
    return MetadataFieldServiceImpl.toMetadata(new HashMap<>(Map.ofEntries(
      Map.entry("@context", List.of("https://w3id.org/kim/amb/context.jsonld", Map.of("@language", "de"))),
      Map.entry("type", new ArrayList<>(List.of("Course", "LearningResource"))),
      Map.entry("id", "https://example.org"),
      Map.entry("name", "name"),
      Map.entry("description", "description"),
      Map.entry("keywords", new ArrayList<>(List.of("Gitlab", "Multimedia"))),
      Map.entry("inLanguage", new ArrayList<>(List.of("en"))),
      Map.entry("license", Map.of("id", "https://creativecommons.org/licenses/by/4.0/")),
      Map.entry("creator", new ArrayList<>(List.of(
        Map.of(
          "type", "Person",
          "name", "GivenName FamilyName",
          "affiliation", Map.of("name", "name", "type", "Organization")
        ),
        Map.of(
          "type", "Organization",
          "name", "name",
          "id", "https://example.org/ror"
        )
      ))),
      Map.entry("caption", new ArrayList<>(List.of(
        Map.of(
          "type", "MediaObject",
          "id", "https://example.org/subs-en.vtt",
          "inLanguage", "en",
          "encodingFormat", "text/vtt"
        )
      ))),
      Map.entry("contributor", new ArrayList<>(List.of(
        Map.of(
          "type", "Person",
          "name", "Jane Doe",
          "honorificPrefix", "Dr.",
          "affiliation", Map.of("name", "name", "type", "Organization")
        )
      ))),
      Map.entry("conditionsOfAccess", Map.of("id", "https://w3id.org/kim/conditionsOfAccess/no_login")),
      Map.entry("dateCreated", "2020-04-08"),
      Map.entry("duration", "PT47M58S"),
      Map.entry("audience", new ArrayList<>(List.of(
        new HashMap<>(Map.of(
          "id", "http://purl.org/dcx/lrmi-vocabs/educationalAudienceRole/testaudience",
          "prefLabel", Map.of("de", "Lernender", "en", "student")
        ))
      ))),
      Map.entry("learningResourceType", new ArrayList<>(List.of(
        new HashMap<>(Map.of(
          "id", "https://w3id.org/kim/hcrt/testType",
          "prefLabel", Map.of("de", "Kurs", "en", "course")
        ))
      ))),
      Map.entry("about", new ArrayList<>(List.of(
        new HashMap<>(Map.of(
          "id", "https://w3id.org/kim/hochschulfaechersystematik/testsubject",
          "prefLabel", Map.of("de", "Mathematik", "en", "mathematics")
        ))
      ))),
      Map.entry("mainEntityOfPage", new ArrayList<>(List.of(
        Map.of(
          "id", "https://example.org/desc/123",
          "provider", Map.of("id", "https://example.org/provider/testprovider", "name", "testname")
        )
      ))),
      Map.entry("publisher", new ArrayList<>(List.of(
        Map.of("name", "publisher", "id", "https://example.org/desc/123", "type", "Organization")
      ))),
      Map.entry("sourceOrganization", new ArrayList<>(List.of(
        Map.of("name", "sourceOrganization", "type", "Organization")
      ))),
      Map.entry("trailer", Map.of("type", "VideoObject", "embedUrl", "https://example.org/trailer")),
      Map.entry("assesses", List.of(Map.of("id", "https://example.org/assesses/1", "prefLabel", Map.of("de", "Deutsch", "en", "English")))),
      Map.entry("competencyRequired", List.of(Map.of("id", "https://example.org/competencies/2", "prefLabel", Map.of("de", "Deutsch", "en", "English")))),
      Map.entry("educationalLevel", List.of(Map.of("id", "https://w3id.org/kim/educationalLevel/level_A", "prefLabel", Map.of("de", "Deutsch", "en", "English")))),
      Map.entry("teaches", List.of(Map.of("id", "https://example.org/teaches/1", "prefLabel", Map.of("de", "Deutsch", "en", "English"))))
    )), "id");
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
        .andExpect(jsonPath("$.id").value(metadata.get("id")));
  }

  @Test
  void testGetRequestWithNonExistingData() throws Exception {
    mvc.perform(get(METADATA_CONTROLLER_BASE_PATH + "/1000")).andExpect(status().isNotFound());
  }

  @Test
  void testInvalidContextUri() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.put("@context", List.of(3, Map.of("@language", "de")));

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isBadRequest());

    // put request
    BackendMetadata existingMetadata = createTestMetadata();
    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isBadRequest());
  }

  @Test
  void testInvalidContextLanguage() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.put("@context", List.of("https://w3id.org/kim/amb/context.jsonld", Map.of("invalid", "de")));

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isBadRequest());

    metadata.put("@context", List.of("https://w3id.org/kim/amb/context.jsonld", "invalid"));

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isBadRequest());
  }

  @Test
  void testPostRequest() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
        .andExpect(content().json(
            "{\"keywords\":[\"Gitlab\",\"Multimedia\"],\"about\":[{\"prefLabel\":{\"de\":\"Mathematik\",\"en\":\"mathematics\"},\"id\":\"https://w3id.org/kim/hochschulfaechersystematik/testsubject\"}],\"caption\":[{\"inLanguage\":\"en\",\"encodingFormat\":\"text/vtt\",\"id\":\"https://example.org/subs-en.vtt\",\"type\":\"MediaObject\"}],\"description\":\"description\",\"type\":[\"Course\",\"LearningResource\"],\"mainEntityOfPage\":[{\"provider\":{\"name\":\"testname\",\"id\":\"https://example.org/provider/testprovider\"},\"id\":\"https://example.org/desc/123\"}],\"competencyRequired\":[{\"prefLabel\":{\"de\":\"Deutsch\",\"en\":\"English\"},\"id\":\"https://example.org/competencies/2\"}],\"conditionsOfAccess\":{\"id\":\"https://w3id.org/kim/conditionsOfAccess/no_login\"},\"duration\":\"PT47M58S\",\"trailer\":{\"embedUrl\":\"https://example.org/trailer\",\"type\":\"VideoObject\"},\"teaches\":[{\"prefLabel\":{\"de\":\"Deutsch\",\"en\":\"English\"},\"id\":\"https://example.org/teaches/1\"}],\"dateCreated\":\"2020-04-08\",\"assesses\":[{\"prefLabel\":{\"de\":\"Deutsch\",\"en\":\"English\"},\"id\":\"https://example.org/assesses/1\"}],\"contributor\":[{\"affiliation\":{\"name\":\"name\",\"type\":\"Organization\"},\"honorificPrefix\":\"Dr.\",\"name\":\"Jane Doe\",\"type\":\"Person\"}],\"id\":\"https://example.org\",\"learningResourceType\":[{\"prefLabel\":{\"de\":\"Kurs\",\"en\":\"course\"},\"id\":\"https://w3id.org/kim/hcrt/testType\"}],\"educationalLevel\":[{\"prefLabel\":{\"de\":\"Deutsch\",\"en\":\"English\"},\"id\":\"https://w3id.org/kim/educationalLevel/level_A\"}],\"audience\":[{\"prefLabel\":{\"de\":\"Lernender\",\"en\":\"student\"},\"id\":\"http://purl.org/dcx/lrmi-vocabs/educationalAudienceRole/testaudience\"}],\"creator\":[{\"name\":\"GivenName FamilyName\",\"affiliation\":{\"name\":\"name\",\"type\":\"Organization\"},\"type\":\"Person\"},{\"name\":\"name\",\"id\":\"https://example.org/ror\",\"type\":\"Organization\"}],\"inLanguage\":[\"en\"],\"@context\":[\"https://w3id.org/kim/amb/context.jsonld\",{\"@language\":\"de\"}],\"license\":{\"id\":\"https://creativecommons.org/licenses/by/4.0/\"},\"name\":\"name\",\"sourceOrganization\":[{\"name\":\"sourceOrganization\",\"type\":\"Organization\"}],\"publisher\":[{\"name\":\"publisher\",\"id\":\"https://example.org/desc/123\",\"type\":\"Organization\"}],\"isAccessibleForFree\":true}"));
  }

  @Test
  void testPostRequestWithPublicIndex() throws Exception {
    BackendConfig initialConfig = setupPublicIndices();
    Map<String, Object> metadata = getTestMetadataDto();

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk());

    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getMetadataIndexName())));
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getExtendedMetadataIndexName())));
  }

  @Test
  void testBulkUpdate() throws Exception {
    Map<String, Object> metadata1 = getTestMetadataDto();
    Map<String, Object> metadata2 = getTestMetadataDto();
    metadata2.put("id", "https://example.org/record2");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH + "/bulk").contentType(MediaType.APPLICATION_JSON)
        .content(asJson(List.of(metadata1, metadata2)))).andExpect(status().isOk())
      .andExpect(content().json("{\"success\":2,\"failed\":0,\"messages\":[]}"));
  }

  @Test
  void testBulkUpdateWithFailure() throws Exception {
    Map<String, Object> metadata1 = getTestMetadataDto();
    Map<String, Object> metadata2 = getTestMetadataDto();
    metadata2.put("id", "https://example.org/record2");
    metadata2.put("image", "invalid url");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH + "/bulk").contentType(MediaType.APPLICATION_JSON)
        .content(asJson(List.of(metadata1, metadata2)))).andExpect(status().isOk())
      .andExpect(content().json("{\"success\":1,\"failed\":1}"));
  }

  @Test
  void testBulkUpdateWithMissingRequiredParameter() throws Exception {
    Map<String, Object> metadata1 = getTestMetadataDto();
    Map<String, Object> metadata2 = getTestMetadataDto();
    metadata2.remove("id");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH + "/bulk").contentType(MediaType.APPLICATION_JSON)
        .content(asJson(List.of(metadata1, metadata2)))).andExpect(status().isBadRequest());
  }

  @Test
  void testPostRequestUpdatePrefLabel() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    List<Map<String, Object>> learningResourceTypes = MetadataHelper.parseList(metadata, "learningResourceType", new TypeReference<>() {});
    Assertions.assertNotNull(learningResourceTypes);
    learningResourceTypes.get(0).put("prefLabel", Map.of("de", "test"));
    metadata.put("learningResourceType", learningResourceTypes);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
        .andExpect(content().json(
            "{\"learningResourceType\":[{\"id\":\"https://w3id.org/kim/hcrt/testType\",\"prefLabel\":{\"de\":\"test\"}}]}"));
  }

  @Test
  void testPostRequestAddVocabParentForFlatField() throws Exception {
    configRepository.deleteAll();
    BackendConfig initialConfig = new BackendConfig();
    BackendConfig.FieldProperties fieldProperties = new BackendConfig.FieldProperties();
    fieldProperties.setFieldName("flatType");
    fieldProperties.setVocabIdentifier("hcrt");
    fieldProperties.setAddMissingVocabParents(true);
    initialConfig.setFieldProperties(List.of(fieldProperties));
    configRepository.save(initialConfig);
    vocabItemRepository.deleteAll();
    VocabItem vocabItem = new VocabItem();
    vocabItem.setVocabIdentifier("hcrt");
    vocabItem.setItemKey("https://w3id.org/kim/hcrt/testType");
    vocabItem.setParentKey("https://w3id.org/kim/hcrt/testType2");
    vocabItemRepository.save(vocabItem);

    Map<String, Object> metadata = getTestMetadataDto();
    List<String> learningResourceTypes = List.of("https://w3id.org/kim/hcrt/testType");
    metadata.put("flatType", learningResourceTypes);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(metadata))).andExpect(status().isOk())
            .andExpect(content().json(
                    "{\"flatType\":[\"https://w3id.org/kim/hcrt/testType\", \"https://w3id.org/kim/hcrt/testType2\"]}"));
  }

  @Test
  void testPostRequestCreateMultipleLearningResourceTypes() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    List<Map<String, Object>> learningResourceTypes = MetadataHelper.parseList(metadata, "learningResourceType", new TypeReference<>() {});
    Assertions.assertNotNull(learningResourceTypes);
    learningResourceTypes.add(new HashMap<>(Map.of("id", "https://w3id.org/kim/hcrt/testType2")));
    metadata.put("learningResourceType", learningResourceTypes);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
        .andExpect(content().json(
            "{\"learningResourceType\":[{\"id\":\"https://w3id.org/kim/hcrt/testType\"}, {\"id\":\"https://w3id.org/kim/hcrt/testType2\"}]}"));
  }

  @Test
  void testPostRequestCreateMultipleAudiences() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    List<Map<String, Object>> audiences = MetadataHelper.parseList(metadata, "audience", new TypeReference<>() {});
    Assertions.assertNotNull(audiences);
    audiences.add(new HashMap<>(Map.of("id", "http://purl.org/dcx/lrmi-vocabs/educationalAudienceRole/testaudience2")));
    metadata.put("audience", audiences);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
      .content(asJson(metadata))).andExpect(status().isOk())
      .andExpect(content().json(
        "{\"audience\":[{\"id\":\"http://purl.org/dcx/lrmi-vocabs/educationalAudienceRole/testaudience\"}, {\"id\":\"http://purl.org/dcx/lrmi-vocabs/educationalAudienceRole/testaudience2\"}]}"));
  }

  @Test
  void testPostRequestCreateMultipleLanguages() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.put("inLanguage", List.of("en", "fr"));

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
      .content(asJson(metadata))).andExpect(status().isOk())
      .andExpect(content().json(
        "{\"inLanguage\":[\"en\", \"fr\"]}"));
  }

  @Test
  void testPostRequestWithExistingDataNullData() throws Exception {
    createTestMetadata();
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.remove("about");
    metadata.remove("assesses");
    metadata.remove("audience");
    metadata.remove("caption");
    metadata.remove("competencyRequired");
    metadata.remove("creator");
    metadata.remove("contributor");
    metadata.remove("educationalLevel");
    metadata.remove("keywords");
    metadata.remove("learningResourceType");
    metadata.remove("teaches");
    metadata.remove("type");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
      .content(asJson(metadata))).andExpect(status().isOk())
      .andExpect(content().json(
            "{\"id\":\"https://example.org\",\"name\":\"name\",\"description\":\"description\",\"license\":{\"id\":\"https://creativecommons.org/licenses/by/4.0/\"},\"dateCreated\":\"2020-04-08\",\"inLanguage\":[\"en\"]}"))
      .andExpect(jsonPath("$.about").doesNotExist())
      .andExpect(jsonPath("$.assesses").doesNotExist())
      .andExpect(jsonPath("$.audience").doesNotExist())
      .andExpect(jsonPath("$.caption").doesNotExist())
      .andExpect(jsonPath("$.competencyRequired").doesNotExist())
      .andExpect(jsonPath("$.creator").doesNotExist())
      .andExpect(jsonPath("$.contributor").doesNotExist())
      .andExpect(jsonPath("$.educationalLevel").doesNotExist())
      .andExpect(jsonPath("$.keywords").doesNotExist())
      .andExpect(jsonPath("$.learningResourceType").doesNotExist())
      .andExpect(jsonPath("$.teaches").doesNotExist());
  }

  @Test
  void testPostRequestWithExistingEmptyType() throws Exception {
    createTestMetadata();
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.put("type", new ArrayList<>());

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
      .content(asJson(metadata))).andExpect(status().isOk())
      .andExpect(content().json(
        "{\"type\":[\"LearningResource\"]}"));
  }

  @Test
  void testPostRequestWithInvalidId() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.put("id", "this is no uri");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
      .content(asJson(metadata)))
      .andExpect(result -> Assertions.assertInstanceOf(IllegalArgumentException.class, result.getResolvedException()));
  }

  @Test
  void testPostRequestWithInvalidPrefLabel() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    List<Map<String, Object>> learningResourceTypes = MetadataHelper.parseList(metadata, "learningResourceType", new TypeReference<>() {});
    Assertions.assertNotNull(learningResourceTypes);
    learningResourceTypes.get(0).put("prefLabel", Map.of("invalid code", "value"));
    metadata.put("learningResourceType", learningResourceTypes);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
      .content(asJson(metadata)))
      .andExpect(result -> Assertions.assertInstanceOf(IllegalArgumentException.class, result.getResolvedException()));
  }

  @Test
  void testPostRequestWithMissingRequiredParameter() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.remove("id");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isBadRequest());
  }

  @Test
  void testPutRequest() throws Exception {
    BackendMetadata existingMetadata = createTestMetadata();
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.put("mainEntityOfPage", new ArrayList<>(List.of(
      Map.of(
        "id", "https://example2.org/desc/123",
        "provider", Map.of("id", "http://example.org/provider/testprovider2", "name", "testname2")
      )
    )));

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON).content(asJson(metadata)))
        .andExpect(status().isOk()).andExpect(content().json(
            "{\"id\":\"https://example.org\",\"mainEntityOfPage\":[{\"id\":\"https://example.org/desc/123\"}, {\"id\":\"https://example2.org/desc/123\"}]}"));

    Assertions.assertEquals(1, repository.count());
  }

  @Test
  void testPutRequestWithMissingRequiredParameter() throws Exception {
    BackendMetadata existingMetadata = createTestMetadata();
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.remove("id");

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON).content(asJson(metadata)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testPutRequestWithNonExistingData() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/1").contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isNotFound());
  }

  @Test
  void testDeleteRequest() throws Exception {
    BackendMetadata existingMetadata = createTestMetadata();

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteRequestWithPublicIndex() throws Exception {
    BackendConfig initialConfig = setupPublicIndices();
    BackendMetadata existingMetadata = createTestMetadata();
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getMetadataIndexName())));
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getExtendedMetadataIndexName())));

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
      .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    assertEquals(0, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getMetadataIndexName())));
    assertEquals(0, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getExtendedMetadataIndexName())));
  }

  @Test
  void testDeleteAllRequest() throws Exception {
    createTestMetadata();

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH)
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteAllWithPublicIndexRequest() throws Exception {
    BackendConfig initialConfig = setupPublicIndices();
    createTestMetadata();
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getMetadataIndexName())));
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getExtendedMetadataIndexName())));

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH)
      .contentType(MediaType.APPLICATION_JSON).param("update-public", "true"))
      .andExpect(status().isOk());

    Assertions.assertEquals(0, repository.count());
    assertEquals(0, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getMetadataIndexName())));
    assertEquals(0, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getExtendedMetadataIndexName())));
  }

  @Test
  void testDeleteAllAndKeepPublicIndexRequest() throws Exception {
    BackendConfig initialConfig = setupPublicIndices();
    createTestMetadata();
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getMetadataIndexName())));
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getExtendedMetadataIndexName())));

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH)
        .contentType(MediaType.APPLICATION_JSON).param("update-public", "false"))
      .andExpect(status().isOk());

    Assertions.assertEquals(0, repository.count());
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getMetadataIndexName())));
    assertEquals(1, elasticsearchOperations.count(elasticsearchOperations.matchAllQuery(), IndexCoordinates.of(initialConfig.getExtendedMetadataIndexName())));
  }
  @Test
  void testDeleteByProviderName() throws Exception {
    createTestMetadata();

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source")
      .contentType(MediaType.APPLICATION_JSON).content("{\"queryName\": \"providerName\", \"queryParam\": \"testname\"}"))
      .andExpect(status().isOk());

    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteMainEntityOfPageByProviderNameAndKeepMetadata() throws Exception {
    BackendMetadata metadata = getTestMetadata();
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(metadata.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    mainEntityOfPage.add(Map.of(
      "id", "https://example2.org/desc/123",
      "provider", Map.of("id", "https://example.org/provider/testprovider2", "name", "testname2")
    ));
    metadata.getData().put("mainEntityOfPage", mainEntityOfPage);
    repository.save(metadata);

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source")
        .contentType(MediaType.APPLICATION_JSON).content("{\"queryName\": \"providerName\", \"queryParam\": \"testname\"}"))
      .andExpect(status().isOk());

    Assertions.assertEquals(1, repository.count());
    List<Map<String, Object>> resultMainEntityOfPage = MetadataHelper.parseList(repository.findAll().iterator().next().getData(), "mainEntityOfPage", new TypeReference<>() {});
    Assertions.assertEquals(List.of(Map.of(
            "id", "https://example2.org/desc/123",
            "provider", Map.of("id", "https://example.org/provider/testprovider2", "name", "testname2")
    )), resultMainEntityOfPage);
  }

  @Test
  void testDeleteRequestWithNonExistingData() throws Exception {
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDatesWithoutTime() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.put("dateCreated", "2020-04-08");
    metadata.put("datePublished", "2022-07-08");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
      .andExpect(content().json("{\"dateCreated\": \"2020-04-08\", \"datePublished\": \"2022-07-08\"}"));
  }

  @Test
  void testDatesWithTime() throws Exception {
    Map<String, Object> metadata = getTestMetadataDto();
    metadata.put("dateCreated", "2020-04-08T10:00:00Z");
    metadata.put("datePublished", "2022-07-08T12:34:56Z");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
      .andExpect(content().json("{\"dateCreated\": \"2020-04-08T10:00:00Z\", \"datePublished\": \"2022-07-08T12:34:56Z\"}"));
  }

  @Test
  void testDeleteMainEntityOfPageRequest() throws Exception {
    BackendMetadata metadata = createTestMetadata();
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(metadata.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    String encodedIdentifier = Base64.getUrlEncoder().encodeToString(((String) mainEntityOfPage.get(0).get("id")).getBytes(StandardCharsets.UTF_8));
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source/" + encodedIdentifier))
      .andExpect(status().isOk());
    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteMainEntityOfPageForNonExistingDataRequest() throws Exception {
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(getTestMetadataDto(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    String encodedIdentifier = Base64.getUrlEncoder().encodeToString(((String) mainEntityOfPage.get(0).get("id")).getBytes(StandardCharsets.UTF_8));
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source/" + encodedIdentifier))
      .andExpect(status().isNotFound());
  }

  @Test
  void testDeleteMainEntityOfPageAndKeepRemainingMetadataRequest() throws Exception {
    BackendMetadata metadata = getTestMetadata();
    List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(metadata.getData(), "mainEntityOfPage", new TypeReference<>() {});
    assertNotNull(mainEntityOfPage);
    mainEntityOfPage.add(Map.of(
        "id", "https://example2.org/desc/123",
        "provider", Map.of("id", "https://example.org/provider/testprovider2", "name", "testname2")
    ));
    metadata.getData().put("mainEntityOfPage", mainEntityOfPage);
    repository.save(metadata);

    String encodedIdentifier = Base64.getUrlEncoder().encodeToString(((String) mainEntityOfPage.get(0).get("id")).getBytes(StandardCharsets.UTF_8));
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source/" + encodedIdentifier))
      .andExpect(status().isOk());
    Assertions.assertEquals(1, repository.count());
  }

  @Test
  void testDeleteMainEntityOfPageInvalidRequest() throws Exception {
    String encodedIdentifier = Base64.getUrlEncoder().encodeToString("invalid invalid".getBytes(StandardCharsets.UTF_8));
    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/source/" + encodedIdentifier))
      .andExpect(status().isBadRequest());
  }
}
