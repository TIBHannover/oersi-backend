package eu.tib.oersi.controller;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.tib.oersi.domain.About;
import eu.tib.oersi.domain.Audience;
import eu.tib.oersi.domain.Creator;
import eu.tib.oersi.domain.LearningResourceType;
import eu.tib.oersi.domain.LocalizedString;
import eu.tib.oersi.domain.MainEntityOfPage;
import eu.tib.oersi.domain.Metadata;
import eu.tib.oersi.domain.SourceOrganization;
import eu.tib.oersi.dto.LocalizedStringDto;
import eu.tib.oersi.dto.MetadataDto;
import eu.tib.oersi.repository.MetadataRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
class MetadataControllerTest {

  /** base path of the {@link MetadataController} */
  private static final String METADATA_CONTROLLER_BASE_PATH = "/api/metadata";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private MetadataRepository repository;

  @Autowired
  private ModelMapper modelMapper;

  @AfterEach
  void cleanup() {
    repository.deleteAll();
    repository.flush();
  }

  private static String asJson(final Object obj) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
      @Override
      public void serialize(final OffsetDateTime offsetDateTime, final JsonGenerator jsonGenerator,
          final SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime));
      }
    });
    objectMapper.registerModule(simpleModule);
    return objectMapper.writeValueAsString(obj);
  }

  private Metadata createTestMetadata() {
    return repository.saveAndFlush(getTestMetadata());
  }

  private Metadata getTestMetadata() {
    Metadata metadata = new Metadata();

    Creator author = new Creator();
    author.setType("Person");
    author.setName("GivenName FamilyName");
    Creator institution = new Creator();
    institution.setType("Organization");
    institution.setName("name");
    metadata.setCreator(List.of(author, institution));

    Audience audience = new Audience();
    audience.setIdentifier("audience");
    LocalizedString audiencePrefLabel = new LocalizedString();
    audiencePrefLabel.setLocalizedStrings(Map.of("de", "Lernender", "en", "student"));
    audience.setPrefLabel(audiencePrefLabel);
    metadata.setAudience(audience);

    MainEntityOfPage mainEntityOfPage = new MainEntityOfPage();
    mainEntityOfPage.setIdentifier("http://example.url/desc/123");
    metadata.setMainEntityOfPage(new ArrayList<>(List.of(mainEntityOfPage)));

    SourceOrganization sourceOrganization = new SourceOrganization();
    sourceOrganization.setName("sourceOrganization");
    metadata.setSourceOrganization(new ArrayList<>(List.of(sourceOrganization)));

    LearningResourceType learningResourceType = new LearningResourceType();
    learningResourceType.setIdentifier("learningResourceType");
    LocalizedString lrtPrefLabel = new LocalizedString();
    lrtPrefLabel.setLocalizedStrings(Map.of("de", "Kurs", "en", "course"));
    learningResourceType.setPrefLabel(lrtPrefLabel);
    metadata.setLearningResourceType(learningResourceType);

    About about = new About();
    about.setIdentifier("subject");
    LocalizedString aboutPrefLabel = new LocalizedString();
    aboutPrefLabel.setLocalizedStrings(Map.of("de", "Mathematik", "en", "mathematics"));
    about.setPrefLabel(aboutPrefLabel);
    metadata.setAbout(List.of(about));

    metadata.setDescription("description");
    metadata.setInLanguage("en");
    metadata.setLicense("https://creativecommons.org/licenses/by/4.0/deed.de");
    metadata.setName("name");
    metadata.setIdentifier("http://example.url");

    metadata.setDateCreated(LocalDate.of(2020, 4, 8));

    metadata.setDateModifiedInternal(LocalDateTime.now());
    return metadata;
  }

  private MetadataDto getTestMetadataDto() {
    return modelMapper.map(getTestMetadata(), MetadataDto.class);
  }

  @Test
  void testGetRequest() throws Exception {
    Metadata metadata = createTestMetadata();

    mvc.perform(get(METADATA_CONTROLLER_BASE_PATH + "/" + metadata.getId()))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value(metadata.getName()))
        .andExpect(jsonPath("$.id").value(metadata.getIdentifier()));
  }

  @Test
  void testGetRequestWithNonExistingData() throws Exception {
    mvc.perform(get(METADATA_CONTROLLER_BASE_PATH + "/1000")).andExpect(status().isBadRequest());
  }

  @Test
  void testPostRequest() throws Exception {
    MetadataDto metadata = getTestMetadataDto();

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
        .andExpect(content().json(
            "{\"id\":\"http://example.url\",\"name\":\"name\",\"creator\":[{\"name\":\"GivenName FamilyName\",\"type\":\"Person\"},{\"name\":\"name\",\"type\":\"Organization\"}],\"description\":\"description\",\"about\":[{\"id\":\"subject\",\"prefLabel\":{\"de\":\"Mathematik\",\"en\":\"mathematics\"}}],\"license\":\"https://creativecommons.org/licenses/by/4.0/deed.de\",\"dateCreated\":\"2020-04-08\",\"inLanguage\":\"en\",\"learningResourceType\":{\"id\":\"learningResourceType\",\"prefLabel\":{\"de\":\"Kurs\",\"en\":\"course\"}},\"audience\":{\"id\":\"audience\",\"prefLabel\":{\"de\":\"Lernender\",\"en\":\"student\"}},\"mainEntityOfPage\":[{\"id\":\"http://example.url/desc/123\"}], \"sourceOrganization\":[{\"name\":\"sourceOrganization\"}]}"));
  }

  @Test
  void testPostRequestWithLongName() throws Exception {
    MetadataDto metadata = getTestMetadataDto();
    metadata.setName("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lore Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lore");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk());
  }
  
  @Test
  void testPostRequestWithLongDescription() throws Exception {
    MetadataDto metadata = getTestMetadataDto();
    metadata.setDescription("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. \n" + 
        "\n" + 
        "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. \n" + 
        "\n" + 
        "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. \n" + 
        "\n" + 
        "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. \n" + 
        "\n" + 
        "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis. \n" + 
        "\n" + 
        "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, At accusam aliquyam diam diam dolore dolores duo eirmod eos erat, et nonumy sed tempor et et invidunt justo labore Stet clita ea et gubergren, kasd magna no rebum. sanctus sea sed takimata ut vero voluptua. est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat. \n" + 
        "\n" + 
        "Consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. \n" + 
        "\n" + 
        "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facili");

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk());
  }
  
  @Test
  void testPostRequestUpdatePrefLabel() throws Exception {
    MetadataDto metadata = getTestMetadataDto();
    LocalizedStringDto prefLabel = new LocalizedStringDto();
    prefLabel.put("de", "test");
    metadata.getLearningResourceType().setPrefLabel(prefLabel);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
        .andExpect(content().json(
            "{\"learningResourceType\":{\"id\":\"learningResourceType\",\"prefLabel\":{\"de\":\"test\"}}}"));
  }

  @Test
  void testPostRequestWithExistingDataNullData() throws Exception {
    createTestMetadata();
    MetadataDto metadata = getTestMetadataDto();
    metadata.setAbout(null);
    metadata.setCreator(null);
    metadata.setAudience(null);
    metadata.setMainEntityOfPage(null);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isOk())
        .andExpect(content().json(
            "{\"id\":\"http://example.url\",\"name\":\"name\",\"creator\":[],\"description\":\"description\",\"about\":[],\"license\":\"https://creativecommons.org/licenses/by/4.0/deed.de\",\"dateCreated\":\"2020-04-08\",\"inLanguage\":\"en\",\"learningResourceType\":{\"id\":\"learningResourceType\"},\"audience\":null,\"mainEntityOfPage\":[]}"));
  }

  @Test
  void testPostRequestWithMissingRequiredParameter() throws Exception {
    MetadataDto metadata = getTestMetadataDto();
    metadata.setId(null);

    mvc.perform(post(METADATA_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isBadRequest());
  }

  @Test
  void testPutRequest() throws Exception {
    Metadata existingMetadata = createTestMetadata();
    MetadataDto metadata = getTestMetadataDto();
    metadata.getMainEntityOfPage().get(0).setId("http://example2.url/desc/123");

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON).content(asJson(metadata)))
        .andExpect(status().isOk()).andExpect(content().json(
            "{\"id\":\"http://example.url\",\"name\":\"name\",\"creator\":[{\"name\":\"GivenName FamilyName\",\"type\":\"Person\"},{\"name\":\"name\",\"type\":\"Organization\"}],\"description\":\"description\",\"about\":[{\"id\":\"subject\"}],\"license\":\"https://creativecommons.org/licenses/by/4.0/deed.de\",\"dateCreated\":\"2020-04-08\",\"inLanguage\":\"en\",\"learningResourceType\":{\"id\":\"learningResourceType\"},\"audience\":{\"id\":\"audience\"},\"mainEntityOfPage\":[{\"id\":\"http://example2.url/desc/123\"}]}"));

    Assert.assertEquals(1, repository.count());
  }

  @Test
  void testPutRequestWithMissingRequiredParameter() throws Exception {
    Metadata existingMetadata = createTestMetadata();
    MetadataDto metadata = getTestMetadataDto();
    metadata.setId(null);

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON).content(asJson(metadata)))
        .andExpect(status().isBadRequest());
  }


  @Test
  void testPutRequestWithNonExistingData() throws Exception {
    MetadataDto metadata = getTestMetadataDto();
    metadata.getMainEntityOfPage().get(0).setId("http://example2.url/desc/123");

    mvc.perform(put(METADATA_CONTROLLER_BASE_PATH + "/1").contentType(MediaType.APPLICATION_JSON)
        .content(asJson(metadata))).andExpect(status().isBadRequest());
  }

  @Test
  void testDeleteRequest() throws Exception {
    Metadata existingMetadata = createTestMetadata();

    mvc.perform(delete(METADATA_CONTROLLER_BASE_PATH + "/" + existingMetadata.getId())
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    Assert.assertEquals(0, repository.count());
  }

  @Test
  void testDeleteRequestWithNonExistingData() throws Exception {
    mvc.perform(
        delete(METADATA_CONTROLLER_BASE_PATH + "/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    Assert.assertEquals(0, repository.count());
  }

  @Test
  void testMapperConvertDateToDto() {
    Metadata metadata = new Metadata();
    LocalDate dateCreated = LocalDate.of(2020, 4, 8);
    metadata.setDateCreated(dateCreated);
    MetadataDto dto = modelMapper.map(metadata, MetadataDto.class);
    assertNotNull(dto.getDateCreated());
    Assert.assertEquals(dateCreated, dto.getDateCreated());
  }

  @Test
  void testMapperConvertDateTimeToEntity() {
    MetadataDto metadata = new MetadataDto();
    LocalDate dateCreated = LocalDate.of(2020, 4, 8);
    metadata.setDateCreated(dateCreated);
    Metadata entity = modelMapper.map(metadata, Metadata.class);
    assertNotNull(entity.getDateCreated());
    Assert.assertEquals(dateCreated, entity.getDateCreated());
  }
}
