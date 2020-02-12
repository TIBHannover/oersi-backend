package eu.tib.oersi.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Controller that handles search requests to the OER index.
 * <p>
 * The request will be forwarded to the configured elasticsearch instance with the oer-readonly-user.
 * </p>
 */
@RestController
@RequestMapping(value = SearchController.BASE_PATH)
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
public class SearchController {

  private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

  /** base path of the search controller */
  public static final String BASE_PATH = "/api/search";

  @Autowired
  private RestTemplate restTemplate;

  @Value("${elasticsearch.scheme}")
  private String elasticsearchScheme;

  @Value("${elasticsearch.host}")
  private String elasticsearchHost;

  @Value("${elasticsearch.port}")
  private int elasticsearchPort;

  @Value("${elasticsearch.basepath}")
  private String elasticsearchBasePath;

  @Value("${elasticsearch.oersi_viewer_username}")
  private String elasticsearchUser;

  @Value("${elasticsearch.oersi_viewer_password}")
  private String elasticsearchPassword;

  /**
   * Perform the given GET-request on the configured elasticsearch instance with the configured oer-readonly-user.
   *
   * @param body body of the request
   * @param request request
   * @return response from elasticsearch
   */
  @GetMapping("/**")
  public ResponseEntity<String> processElasticsearchGetRequest(
      @RequestBody(required = false) final String body, final HttpServletRequest request) {
    return processElasticsearchRequest(body, HttpMethod.GET, request);
  }

  /**
   * Perform the given POST-request on the configured elasticsearch instance with the configured oer-readonly-user.
   *
   * @param body body of the request
   * @param request request
   * @return response from elasticsearch
   */
  @PostMapping("/**")
  public ResponseEntity<String> processElasticsearchPostRequest(@RequestBody final String body,
      final HttpServletRequest request) {
    return processElasticsearchRequest(body, HttpMethod.POST, request);
  }

  private ResponseEntity<String> processElasticsearchRequest(final String body,
      final HttpMethod method, final HttpServletRequest request) {
    try {
      URI uri = buildElasticsearchUri(request);
      HttpHeaders headers = buildElasticsearchHeaders(request);
      HttpEntity<String> entity = new HttpEntity<>(body, headers);
      LOG.debug("process elasticsearch {}-request to {}", method, uri);

      return restTemplate.exchange(uri, method, entity, String.class);
    } catch (URISyntaxException e) {
      LOG.error("error while building the elasticsearch URI", e);
      return new ResponseEntity<>("invalid URI", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (HttpStatusCodeException e) {
      return new ResponseEntity<>("elasticsearch request failed: " + e.getStatusText(), e
          .getStatusCode());
    } catch (RestClientException e) {
      LOG.error("error while executing request to elasticsearch", e);
      return new ResponseEntity<>("elasticsearch request failed: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private URI buildElasticsearchUri(final HttpServletRequest originalRequest)
      throws URISyntaxException {
    String requestPath = originalRequest.getRequestURI().replaceFirst(BASE_PATH, "");
    String path = elasticsearchBasePath + requestPath;
    return new URI(elasticsearchScheme, null, elasticsearchHost, elasticsearchPort, path,
        originalRequest.getQueryString(), null);
  }

  private HttpHeaders buildElasticsearchHeaders(final HttpServletRequest originalRequest) {
    HttpHeaders headers = new HttpHeaders();

    // use headers from original request
    Enumeration<String> headerNames = originalRequest.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = originalRequest.getHeader(headerName);
      headers.add(headerName, headerValue);
    }

    headers.add("Authorization", "Basic " + getOerViewerBase64Credentials());

    return headers;
  }

  private String getOerViewerBase64Credentials() {
    String plainCreds = elasticsearchUser + ":" + elasticsearchPassword;
    byte[] plainCredsBytes = plainCreds.getBytes();
    return Base64.getEncoder().encodeToString(plainCredsBytes);
  }

}
