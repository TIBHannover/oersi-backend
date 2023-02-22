package org.oersi.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.oersi.ElasticsearchServicesMock;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(value = "classpath:application.properties")
@Import(ElasticsearchServicesMock.class)
class SearchControllerTest {

  private final static String testPath = "/test";

  @Autowired
  private MockMvc mvc;

  @MockBean
  private RestTemplate restTemplateMock;
  @MockBean
  private JavaMailSender mailSender;

  @Test
  void testGetRequest() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<String>("a result", HttpStatus.OK);
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(
        String.class)))
            .thenReturn(response);

    mvc.perform(get(SearchController.BASE_PATH + testPath))
        .andExpect(status().is(response.getStatusCodeValue()));
  }

  @Test
  void testGetRequestWithQuery() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<String>("a result", HttpStatus.OK);
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(
      String.class)))
      .thenReturn(response);

    mvc.perform(get(SearchController.BASE_PATH + testPath + "?q=test:test"))
      .andExpect(status().is(response.getStatusCodeValue()));
  }

  @Test
  void testPostRequest() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<String>("a result", HttpStatus.OK);
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.POST), isA(HttpEntity.class), eq(
        String.class)))
            .thenReturn(response);

    mvc.perform(post(SearchController.BASE_PATH + testPath).content("{ \"test\":\"test\" }"))
        .andExpect(status().is(response.getStatusCodeValue()));
  }

  @Test
  void testDeleteRequest() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<String>("a result", HttpStatus.OK);
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.DELETE), isA(HttpEntity.class), eq(
      String.class)))
      .thenReturn(response);

    mvc.perform(delete(SearchController.BASE_PATH + testPath).content("{ \"test\":\"test\" }"))
      .andExpect(status().is(response.getStatusCodeValue()));
  }

  @Test
  void testPostRequestWithoutBody() throws Exception {
    ResponseEntity<String> response = new ResponseEntity<String>("a result", HttpStatus.OK);
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.POST), isA(HttpEntity.class), eq(
      String.class)))
      .thenReturn(response);

    mvc.perform(post(SearchController.BASE_PATH + testPath))
      .andExpect(status().is(response.getStatusCodeValue()));
  }

  @Test
  void testHttpStatusCodeException() throws Exception {
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(
        String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden"));
    mvc.perform(get(SearchController.BASE_PATH + testPath))
        .andExpect(status().isForbidden());
  }

  @Test
  void testRestClientException() throws Exception {
    when(restTemplateMock.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(
        String.class)))
            .thenThrow(new ResourceAccessException("No access"));
    mvc.perform(get(SearchController.BASE_PATH + testPath))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void testCorsPreflightRequest() throws Exception {
    mvc.perform(options(SearchController.BASE_PATH + testPath)
      .header("Access-Control-Request-Method", "GET")
      .header("Origin", "https://example.com"))
      .andExpect(status().isOk());
  }

}
