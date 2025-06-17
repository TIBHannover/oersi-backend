package org.sidre.controller;

import org.junit.jupiter.api.Test;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.service.ElasticsearchRequestLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
@ElasticsearchServicesMock
class SearchControllerFeatureLogRequestTest {

  private final static String testPath = "/test";

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private RestTemplate restTemplateMock;
  @MockitoBean
  private JavaMailSender mailSender;
  @MockitoBean
  private ElasticsearchRequestLogService requestLogService;

  @Test
  void testLogRequest() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<String>("a result", HttpStatus.OK);
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.POST), isA(HttpEntity.class), eq(
            String.class)))
            .thenReturn(response);

    mvc.perform(post(SearchController.BASE_PATH + testPath).content("{ \"test\":\"test\" }")
                    .header("User-Agent", "agent abc")
                    .header("referer", "https://example.org")
            )
            .andExpect(status().is(response.getStatusCodeValue()));
    verify(requestLogService, times(1)).logRequest(anyString(), anyString(), anyString(), any(), anyString(), anyString(), anyString());
  }

}
