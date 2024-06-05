package org.sidre.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sidre.domain.ElasticsearchRequestLog;
import org.sidre.repository.ElasticsearchRequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ElasticsearchRequestLogServiceImpl implements ElasticsearchRequestLogService {

    @Data
    public static class ElasticsearchResult {
        @Data
        public static class ElasticsearchResultHits {
            @Data
            public static class ElasticsearchResultHitsTotal {
                private Integer value;
            }
            private ElasticsearchResultHitsTotal total;
        }
        private Long took;
        private ElasticsearchResultHits hits;
    }
    @Data
    public static class ElasticsearchMultiSearchRequest {
        private String header;
        private String body;
    }
    @Data
    public static class ElasticsearchMultiSearchResult {
        private List<ElasticsearchResult> responses;
    }

    private final @NonNull ElasticsearchRequestLogRepository requestLogRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Async
    @Override
    public void logRequest(String body, String method, String path, String urlRequestQueryString, String responseBody, String userAgent, String referer) {
        LocalDateTime timestamp = LocalDateTime.now();
        if (isMultiSearchRequest(path)) {
            List<ElasticsearchMultiSearchRequest> multiSearchRequests = parseMultiSearchRequest(body);
            List<ElasticsearchResult> multiSearchResponses = parseMultiSearchResponseBody(responseBody);
            for (int i = 0; i < multiSearchRequests.size(); i++) {
                ElasticsearchMultiSearchRequest multiSearchRequest = multiSearchRequests.get(i);
                ElasticsearchResult elasticsearchResult = i < multiSearchResponses.size() ? multiSearchResponses.get(i) : null;
                ElasticsearchRequestLog requestLog = initRequestLog(timestamp, multiSearchRequest.body, method, path, urlRequestQueryString, userAgent, referer);
                logSingleRequest(requestLog, elasticsearchResult);
            }
        } else {
            ElasticsearchResult elasticsearchResult = null;
            try {
                elasticsearchResult = objectMapper.readValue(responseBody, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.debug("Cannot parse elasticsearch result");
            }
            ElasticsearchRequestLog requestLog = initRequestLog(timestamp, body, method, path, urlRequestQueryString, userAgent, referer);
            logSingleRequest(requestLog, elasticsearchResult);
        }
    }

    private ElasticsearchRequestLog initRequestLog(LocalDateTime timestamp, String body, String method, String path, String urlRequestQueryString, String userAgent, String referer) {
        ElasticsearchRequestLog requestLog = new ElasticsearchRequestLog();
        requestLog.setTimestamp(timestamp);
        requestLog.setMethod(method);
        requestLog.setPath(path);
        requestLog.setUrlRequestQueryString(urlRequestQueryString);
        requestLog.setBody(body);
        requestLog.setUserAgent(userAgent);
        requestLog.setReferer(referer);
        return requestLog;
    }

    public void logSingleRequest(ElasticsearchRequestLog requestLog, ElasticsearchResult elasticsearchResult) {
        if (elasticsearchResult != null) {
            requestLog.setResultTook(elasticsearchResult.took);
            requestLog.setResultHitsTotal(Optional.ofNullable(elasticsearchResult.hits).map(o -> o.total).map(o -> o.value).orElse(null));
        }
        requestLogRepository.save(requestLog);
    }

    private boolean isMultiSearchRequest(String path) {
        return path.endsWith("/_msearch");
    }

    private List<ElasticsearchMultiSearchRequest> parseMultiSearchRequest(String body) {
        List<ElasticsearchMultiSearchRequest> result = new ArrayList<>();
        String[] lines = body.split("\\r?\\n");
        for (int i = 0; (i + 1) < lines.length; i += 2) {
            ElasticsearchMultiSearchRequest multiSearchRequest = new ElasticsearchMultiSearchRequest();
            multiSearchRequest.setHeader(lines[i]);
            multiSearchRequest.setBody(lines[i + 1]);
            result.add(multiSearchRequest);
        }
        return result;
    }

    private List<ElasticsearchResult> parseMultiSearchResponseBody(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, new TypeReference<ElasticsearchMultiSearchResult>() {}).getResponses();
        } catch (JsonProcessingException e) {
            log.debug("Cannot parse elasticsearch multi search result");
            return new ArrayList<>();
        }
    }
}
