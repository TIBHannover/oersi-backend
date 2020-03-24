package eu.tib.oersi.controller;

import eu.tib.oersi.api.SearchControllerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

/**
 * Controller that handles search requests to the OER index.
 * <p>
 * The request will be forwarded to the configured elasticsearch instance with the oer-readonly-user.
 * </p>
 */
@RestController
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
public class SearchController implements SearchControllerApi {

  private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

  /** base path of the search controller */
  public static final String BASE_PATH = "/api/search";

  private final RestTemplate restTemplate;

  @Value("${elasticsearch.scheme}")
  private String elasticsearchScheme;

  @Value("${elasticsearch.host}")
  private String elasticsearchHost;

  @Value("${elasticsearch.port}")
  private int elasticsearchPort;

  @Value("${elasticsearch.base-path}")
  private String elasticsearchBasePath;

  @Value("${elasticsearch.oersi_viewer_username}")
  private String elasticsearchUser;

  @Value("${elasticsearch.oersi_viewer_password}")
  private String elasticsearchPassword;

  private final HttpServletRequest request;

  @Autowired
  public SearchController(HttpServletRequest httpServletRequest, RestTemplate restTemplate) {
    this.request = httpServletRequest;
    this.restTemplate = restTemplate;
  }


  /**
   * Perform the given GET-request on the configured elasticsearch instance with the configured oer-readonly-user.
   * @param params List of parameters
   * @return response from elasticsearch.
   */
  @Override
  public ResponseEntity<String> processElasticsearchGetRequest(final Object params) {
    return processElasticsearchRequest(params.toString(), HttpMethod.GET, this.request);
  }

  /**
   * Perform the given POST-request on the configured elasticsearch instance with the configured oer-readonly-user.
   *
   * @param body body of the request
   * @return response from elasticsearch.
   */
  @Override
  public ResponseEntity<String> processElasticsearchPostRequest(@RequestBody final String body) {
    return processElasticsearchRequest(body, HttpMethod.POST,this.request);
  }

  /**
   * @param body request come from POST/GET
   * @param method POST/GET
   * @param request HttpServletRequest
   * @return response from elastic search
   */
  private ResponseEntity<String> processElasticsearchRequest(final String body,
      final HttpMethod method, final HttpServletRequest request) {
    try {
      URI uri = buildElasticsearchUri(request);
      HttpHeaders headers = buildElasticsearchHeaders();
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
     String originalPath = (String) originalRequest.getAttribute(
        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    // only for testing from swagger-ui
     if (originalPath.contains("**")) originalPath=originalPath.replace("**","_search");

    String requestPath = originalPath.substring(originalPath.indexOf(BASE_PATH) + BASE_PATH
        .length());
    String path = elasticsearchBasePath + requestPath;
    return new URI(elasticsearchScheme, null, elasticsearchHost, elasticsearchPort, path,
        originalRequest.getQueryString(), null);
  }

  private HttpHeaders buildElasticsearchHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("Authorization", "Basic " + getOerViewerBase64Credentials());
    return headers;
  }

  private String getOerViewerBase64Credentials() {
    String plainCred = elasticsearchUser + ":" + elasticsearchPassword;
    byte[] plainCredBytes = plainCred.getBytes();
    return Base64.getEncoder().encodeToString(plainCredBytes);
  }

}
