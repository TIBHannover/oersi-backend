package eu.tib.oersi.controller;

import eu.tib.oersi.api.SearchControllerApi;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Controller that handles search requests to the OER index.
 * <p>
 * The request will be forwarded to the configured elasticsearch instance with the
 * oer-readonly-user.
 * </p>
 */
@RestController
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SearchController implements SearchControllerApi {

  /** base path of the search controller */
  public static final String BASE_PATH = "/api/search";

  private final @NonNull RestTemplate restTemplate;

  private final @NonNull HttpServletRequest request;

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
   * Perform the given GET-request on the configured elasticsearch instance with the configured
   * oer-readonly-user.
   *
   * @param body body of the request
   * @return response from elasticsearch.
   */
  @Override
  public ResponseEntity<String> processElasticsearchGetRequest(final String body) {
    return processElasticsearchRequest(body, HttpMethod.GET, this.request);
  }

  /**
   * Perform the given POST-request on the configured elasticsearch instance with the configured
   * oer-readonly-user.
   *
   * @param body body of the request
   * @return response from elasticsearch.
   */
  @Override
  public ResponseEntity<String> processElasticsearchPostRequest(@RequestBody final String body) {
    return processElasticsearchRequest(body, HttpMethod.POST, this.request);
  }

  private ResponseEntity<String> processElasticsearchRequest(final String body,
      final HttpMethod method, final HttpServletRequest request) {
    try {
      URI uri = buildElasticsearchUri(request);
      HttpHeaders headers = buildElasticsearchHeaders();
      HttpEntity<String> entity = new HttpEntity<>(body, headers);
      log.debug("process elasticsearch {}-request to {}", method, uri);

      return restTemplate.exchange(uri, method, entity, String.class);
    } catch (URISyntaxException e) {
      log.error("error while building the elasticsearch URI", e);
      return new ResponseEntity<>("invalid URI", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (HttpStatusCodeException e) {
      return new ResponseEntity<>("elasticsearch request failed: " + e.getStatusText(),
          e.getStatusCode());
    } catch (RestClientException e) {
      log.error("error while executing request to elasticsearch", e);
      return new ResponseEntity<>("elasticsearch request failed: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private URI buildElasticsearchUri(final HttpServletRequest originalRequest)
      throws URISyntaxException {
    final String originalPath =
        (String) originalRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    String requestPath =
        originalPath.substring(originalPath.indexOf(BASE_PATH) + BASE_PATH.length());
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
