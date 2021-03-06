package org.oersi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.oersi.controller.SearchController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(properties = {"elasticsearch.scheme="})
@AutoConfigureMockMvc
@TestPropertySource(value = "classpath:application.properties")
public class SearchControllerPropertyOverrideTest {

  private final static String testPath = "/test";

  @Autowired
  private MockMvc mvc;

  @MockBean
  private RestTemplate restTemplateMock;

  @Test
  public void testInvalidUri() throws Exception {
    // uri will be invalid because scheme is empty
    mvc.perform(get(SearchController.BASE_PATH + testPath))
        .andExpect(status().is5xxServerError());
  }

}
