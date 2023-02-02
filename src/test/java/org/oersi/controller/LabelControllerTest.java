package org.oersi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.repository.LabelRepository;
import org.oersi.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ElasticsearchServicesMock.class)
class LabelControllerTest {

  private static final String LABEL_CONTROLLER_BASE_PATH = "/api/label";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private LabelRepository labelRepository;
  @Autowired
  private LabelService labelService;
  @MockBean
  private JavaMailSender mailSender;

  @BeforeEach
  void cleanup() {
    labelRepository.deleteAll();
    labelRepository.flush();
    labelService.clearCache();
  }

  @Test
  void testRetrieveNoneExisting() throws Exception {
    mvc.perform(get(LABEL_CONTROLLER_BASE_PATH + "/en"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void testRetrieveAllExisting() throws Exception {
    labelService.createOrUpdate("en", "key", "value", "lrt");

    mvc.perform(get(LABEL_CONTROLLER_BASE_PATH + "/en"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.key").value("value"));
  }

  @Test
  void testRetrieveGroupExisting() throws Exception {
    labelService.createOrUpdate("en", "key1", "value1", "audience");
    labelService.createOrUpdate("en", "key2", "value2", "lrt");

    mvc.perform(get(LABEL_CONTROLLER_BASE_PATH + "/en").param("vocab", "audience"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.key1").value("value1"));
  }


  @Test
  void testRetrieveNonExistingGroup() throws Exception {
    labelService.createOrUpdate("en", "key2", "value2", "lrt");

    mvc.perform(get(LABEL_CONTROLLER_BASE_PATH + "/en").param("vocab", "audience"))
      .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isEmpty());
  }
}
