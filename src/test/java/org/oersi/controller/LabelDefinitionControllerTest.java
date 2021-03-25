package org.oersi.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oersi.domain.LabelDefinition;
import org.oersi.repository.LabelDefinitionRepository;
import org.oersi.service.LabelDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = {"MANAGE_OERMETADATA"})
class LabelDefinitionControllerTest {

  private static final String LABEL_DEFINITION_CONTROLLER_BASE_PATH = "/api/labeldefinition";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private LabelDefinitionRepository labelDefinitionRepository;
  @Autowired
  private LabelDefinitionService labelDefinitionService;

  @BeforeEach
  void cleanup() {
    labelDefinitionRepository.deleteAll();
    labelDefinitionRepository.flush();
  }

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

  private LabelDefinition getTestData() {
    LabelDefinition definition = new LabelDefinition();
    definition.setIdentifier("XXX");
    return definition;
  }

  private LabelDefinition createTestData() {
    return labelDefinitionService.createOrUpdate(List.of(getTestData())).get(0);
  }

  @Test
  void testRetrieveNoneExisting() throws Exception {
    mvc.perform(get(LABEL_DEFINITION_CONTROLLER_BASE_PATH + "/1"))
      .andExpect(status().isNotFound());
  }

  @Test
  void testRetrieve() throws Exception {
    LabelDefinition existing = createTestData();

    mvc.perform(get(LABEL_DEFINITION_CONTROLLER_BASE_PATH + "/" + existing.getId()))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.identifier").value(existing.getIdentifier()));
  }

  @Test
  void testCreateOrUpdate() throws Exception {
    LabelDefinition data = getTestData();
    mvc.perform(post(LABEL_DEFINITION_CONTROLLER_BASE_PATH).contentType(MediaType.APPLICATION_JSON)
      .content(asJson(data))).andExpect(status().isOk())
      .andExpect(content().json("{\"identifier\": \"XXX\"}"));
  }

  @Test
  void testCreateOrUpdateMany() throws Exception {
    LabelDefinition data1 = getTestData();
    LabelDefinition data2 = getTestData();
    data2.setIdentifier("YYY");
    mvc.perform(post(LABEL_DEFINITION_CONTROLLER_BASE_PATH + "/bulk")
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJson(List.of(data1, data2)))).andExpect(status().isOk())
      .andExpect(content().json("[{\"identifier\": \"XXX\"}, {\"identifier\": \"YYY\"}]"));
  }

  @Test
  void testUpdate() throws Exception {
    LabelDefinition data = createTestData();
    data.setIdentifier("ABC");
    mvc.perform(put(LABEL_DEFINITION_CONTROLLER_BASE_PATH + "/" + data.getId())
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJson(data))).andExpect(status().isOk())
      .andExpect(content().json("{\"identifier\": \"ABC\"}"));
  }

  @Test
  void testUpdateNonExisting() throws Exception {
    LabelDefinition data = getTestData();
    mvc.perform(put(LABEL_DEFINITION_CONTROLLER_BASE_PATH + "/1")
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJson(data))).andExpect(status().isNotFound());
  }

  @Test
  void testDelete() throws Exception {
    LabelDefinition data = createTestData();
    mvc.perform(delete(LABEL_DEFINITION_CONTROLLER_BASE_PATH + "/" + data.getId())
      .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  void testDeleteNonExisting() throws Exception {
    mvc.perform(delete(LABEL_DEFINITION_CONTROLLER_BASE_PATH + "/1")
      .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
  }
}
