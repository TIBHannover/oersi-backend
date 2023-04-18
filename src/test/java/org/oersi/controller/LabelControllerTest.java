package org.oersi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchContainerTest;
import org.oersi.domain.BackendConfig;
import org.oersi.domain.VocabItem;
import org.oersi.repository.VocabItemRepository;
import org.oersi.service.ConfigService;
import org.oersi.service.LabelService;
import org.oersi.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class LabelControllerTest extends ElasticsearchContainerTest {

  private static final String LABEL_CONTROLLER_BASE_PATH = "/api/label";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ConfigService configService;
  @Autowired
  private VocabItemRepository vocabItemRepository;
  @Autowired
  private LabelService labelService;
  @Autowired
  private VocabService vocabService;
  @MockBean
  private JavaMailSender mailSender;

  @BeforeEach
  void setup() {
    vocabItemRepository.deleteAll();
    labelService.clearCache();

    BackendConfig initialConfig = new BackendConfig();
    BackendConfig.FieldProperties audienceProperties = new BackendConfig.FieldProperties();
    audienceProperties.setFieldName("audience");
    audienceProperties.setVocabIdentifier("educationalAudience");
    BackendConfig.FieldProperties learningResourceTypeProperties = new BackendConfig.FieldProperties();
    learningResourceTypeProperties.setFieldName("learningResourceType");
    learningResourceTypeProperties.setVocabIdentifier("hcrt");
    initialConfig.setFieldProperties(List.of(audienceProperties, learningResourceTypeProperties));
    configService.updateMetadataConfig(initialConfig);
  }

  @Test
  void testRetrieveNoneExisting() throws Exception {
    mvc.perform(get(LABEL_CONTROLLER_BASE_PATH + "/en"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isEmpty());
  }

  private void processVocabUpdate(String vocabIdentifier, String key, Map<String, String> prefLabels) {
    VocabItem item = new VocabItem();
    item.setVocabIdentifier(vocabIdentifier);
    item.setItemKey(key);
    item.setPrefLabel(prefLabels);
    vocabService.updateVocab(vocabIdentifier, List.of(item));
  }

  @Test
  void testRetrieveAllExisting() throws Exception {
    processVocabUpdate("hcrt", "key", Map.of("en", "value"));

    mvc.perform(get(LABEL_CONTROLLER_BASE_PATH + "/en"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.key").value("value"));
  }

  @Test
  void testRetrieveFieldExisting() throws Exception {
    processVocabUpdate("educationalAudience", "key1", Map.of("en", "value1"));
    processVocabUpdate("hcrt", "key2", Map.of("en", "value2"));

    mvc.perform(get(LABEL_CONTROLLER_BASE_PATH + "/en").param("field", "audience"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.key1").value("value1"));
  }


  @Test
  void testRetrieveNonExistingField() throws Exception {
    processVocabUpdate("hcrt", "key2", Map.of("en", "value2"));

    mvc.perform(get(LABEL_CONTROLLER_BASE_PATH + "/en").param("field", "audience"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void testRetrieveGroupExisting() throws Exception {
    processVocabUpdate("educationalAudience", "key1", Map.of("en", "value1"));
    processVocabUpdate("hcrt", "key2", Map.of("en", "value2"));

    mvc.perform(get("/api/deprecated/label/en").param("vocab", "audience"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.key1").value("value1"))
            .andExpect(jsonPath("$.key2").doesNotExist());
  }
  @Test
  void testRetrieveNonExistingGroup() throws Exception {
    processVocabUpdate("hcrt", "key2", Map.of("en", "value2"));

    mvc.perform(get("/api/deprecated/label/en").param("vocab", "audience"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void testCorsPreflightRequest() throws Exception {
    mvc.perform(options(LABEL_CONTROLLER_BASE_PATH + "/en")
        .header("Access-Control-Request-Method", "GET")
        .header("Origin", "https://example.com"))
      .andExpect(status().isOk());
  }
}
