package org.oersi.controller;

import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(value = "classpath:application.properties")
@Import(ElasticsearchServicesMock.class)
class OembedControllerTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void testCorsPreflightRequest() throws Exception {
    mvc.perform(options("/api/oembed-json")
      .header("Access-Control-Request-Method", "GET")
      .header("Origin", "https://example.com"))
      .andExpect(status().isOk());
    mvc.perform(options("/api/oembed-xml")
        .header("Access-Control-Request-Method", "GET")
        .header("Origin", "https://example.com"))
      .andExpect(status().isOk());
  }

}
