package org.oersi.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.api.SearchControllerApi;
import org.oersi.service.ElasticsearchRequestLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Controller that handles search requests to the search index.
 * <p>
 * The request will be forwarded to the configured elasticsearch instance with the
 * readonly-user.
 * </p>
 */
@CrossOrigin
@RestController
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
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

  @Value("${elasticsearch.index_viewer_username}")
  private String elasticsearchUser;

  @Value("${elasticsearch.index_viewer_password}")
  private String elasticsearchPassword;
  @Value("${feature.log_elasticsearch_requests}")
  private boolean featureLogRequests;

  private final @NonNull ElasticsearchRequestLogService requestLogService;

  /**
   * Perform the given GET-request on the configured elasticsearch instance with the configured
   * readonly-user.
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
   * readonly-user.
   *
   * @param body body of the request
   * @return response from elasticsearch.
   */
  @Override
  public ResponseEntity<String> processElasticsearchPostRequest(@RequestBody final String body) {
    return processElasticsearchRequest(body, HttpMethod.POST, this.request);
  }

  /**
   * Perform the given DELETE-request on the configured elasticsearch instance with the configured
   * readonly-user.
   *
   * @param body body of the request
   * @return response from elasticsearch.
   */
  @Override
  public ResponseEntity<String> processElasticsearchDeleteRequest(@RequestBody final String body) {
    return processElasticsearchRequest(body, HttpMethod.DELETE, this.request);
  }

  private ResponseEntity<String> processElasticsearchRequest(final String body,
      final HttpMethod method, final HttpServletRequest request) {
    try {
      URI uri = buildElasticsearchUri(request);
      HttpHeaders headers = buildElasticsearchHeaders();
      HttpEntity<String> entity = new HttpEntity<>(body, headers);
      log.debug("process elasticsearch {}-request to {}", method, uri);

      var result = restTemplate.exchange(uri, method, entity, String.class);
      if (featureLogRequests) {
        requestLogService.logRequest(body, method.name(), uri.getPath(), uri.getQuery(), result.getBody(), request.getHeader("user-agent"), request.getHeader("referer"));
      }
      return result;
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
    String queryString = originalRequest.getQueryString();
    if (queryString != null) {
      queryString = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
    }
    return new URI(elasticsearchScheme, null, elasticsearchHost, elasticsearchPort, path,
      queryString, null);
  }

  private HttpHeaders buildElasticsearchHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("Authorization", "Basic " + getViewerBase64Credentials());
    return headers;
  }

  private String getViewerBase64Credentials() {
    String plainCred = elasticsearchUser + ":" + elasticsearchPassword;
    byte[] plainCredBytes = plainCred.getBytes();
    return Base64.getEncoder().encodeToString(plainCredBytes);
  }

}
