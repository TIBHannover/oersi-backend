package org.oersi.controller;

import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.service.ElasticsearchRequestLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"feature.log_elasticsearch_requests=true"})
@AutoConfigureMockMvc
@TestPropertySource(value = "classpath:application.properties")
@Import(ElasticsearchServicesMock.class)
class SearchControllerFeatureLogRequestTest {

  private final static String testPath = "/test";

  @Autowired
  private MockMvc mvc;

  @MockBean
  private RestTemplate restTemplateMock;
  @MockBean
  private JavaMailSender mailSender;
  @MockBean
  private ElasticsearchRequestLogService requestLogService;

  @Test
  void testLogRequest() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<String>("a result", HttpStatus.OK);
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.POST), isA(HttpEntity.class), eq(
            String.class)))
            .thenReturn(response);

    mvc.perform(post(SearchController.BASE_PATH + testPath).content("{ \"test\":\"test\" }"))
            .andExpect(status().is(response.getStatusCodeValue()));
    verify(requestLogService, times(1)).logRequest(anyString(), anyString(), anyString(), anyString());
  }

}
