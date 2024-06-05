package org.sidre.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchContainerTest;
import org.sidre.domain.BackendConfig;
import org.sidre.dto.ConfigDto;
import org.sidre.repository.BackendConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

}
