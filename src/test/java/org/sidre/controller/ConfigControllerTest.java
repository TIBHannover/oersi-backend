package org.sidre.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchContainerTest;
import org.sidre.domain.BackendConfig;
import org.sidre.dto.ConfigDefaultFieldPropertiesDto;
import org.sidre.dto.ConfigDto;
import org.sidre.dto.ConfigFieldPropertiesDto;
import org.sidre.repository.BackendConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(roles = {"MANAGE_METADATA"})
class ConfigControllerTest extends ElasticsearchContainerTest {

  private static final String CONTROLLER_BASE_PATH = "/api/metadata-config";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private BackendConfigRepository configRepository;

  private static String asJson(final Object obj) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(obj);
  }


  @Test
  void createConfig() throws Exception {
    ConfigDto config = new ConfigDto();
    config.setMetadataIndexName("index1");
    config.setExtendedMetadataIndexName("index2");
    mvc.perform(post(CONTROLLER_BASE_PATH)
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJson(config))).andExpect(status().isOk());

    assertEquals(1, configRepository.count());
  }

  @Test
  void updateConfig() throws Exception {
    BackendConfig initialConfig = new BackendConfig();
    initialConfig.setMetadataIndexName("old1");
    initialConfig.setExtendedMetadataIndexName("old2");
    configRepository.save(initialConfig);

    ConfigDto config = new ConfigDto();
    config.setMetadataIndexName("index1");
    config.setExtendedMetadataIndexName("index2");
    mvc.perform(post(CONTROLLER_BASE_PATH)
      .contentType(MediaType.APPLICATION_JSON)
      .content(asJson(config))).andExpect(status().isOk());

    assertEquals(1, configRepository.count());
    BackendConfig backendConfig = configRepository.findById("search_index_backend_config").orElse(null);
    assertNotNull(backendConfig);
    assertEquals(config.getMetadataIndexName(), backendConfig.getMetadataIndexName());
    assertEquals(config.getExtendedMetadataIndexName(), backendConfig.getExtendedMetadataIndexName());
  }

  @Test
  void createFieldPropertiesConfig() throws Exception {
    ConfigDto config = getConfigDto();
    mvc.perform(post(CONTROLLER_BASE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJson(config))).andExpect(status().isOk());

    assertEquals(1, configRepository.count());

    BackendConfig backendConfig = configRepository.findById("search_index_backend_config").orElse(null);
    assertNotNull(backendConfig);
    assertNotNull(backendConfig.getFieldProperties());
    assertEquals("id", backendConfig.getFieldProperties().get(0).getVocabItemIdentifierField());
    assertEquals("prefLabel", backendConfig.getFieldProperties().get(0).getVocabItemLabelField());
  }

  private static @NotNull ConfigDto getConfigDto() {
    ConfigDto config = new ConfigDto();
    ConfigDefaultFieldPropertiesDto defaultFieldProperties = new ConfigDefaultFieldPropertiesDto();
    defaultFieldProperties.setVocabItemIdentifierField("id");
    defaultFieldProperties.setVocabItemLabelField("prefLabel");
    config.setDefaultFieldProperties(defaultFieldProperties);
    ConfigFieldPropertiesDto fieldProperties = new ConfigFieldPropertiesDto();
    fieldProperties.setFieldName("about");
    fieldProperties.setVocabIdentifier("hochschulfaechersystematik");
    fieldProperties.setAddMissingVocabParents(true);
    config.setFieldProperties(List.of(fieldProperties));
    return config;
  }

}
