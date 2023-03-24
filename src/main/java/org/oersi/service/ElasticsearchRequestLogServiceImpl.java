package org.oersi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oersi.domain.ElasticsearchRequestLog;
import org.oersi.repository.ElasticsearchRequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    private final @NonNull ElasticsearchRequestLogRepository requestLogRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Async
    @Override
    public void logRequest(String body, String method, String path, String responseBody, String userAgent, String referer) {
        ElasticsearchRequestLog requestLog = new ElasticsearchRequestLog();
        requestLog.setMethod(method);
        requestLog.setPath(path);
        requestLog.setBody(body);
        requestLog.setUserAgent(userAgent);
        requestLog.setReferer(referer);
        try {
            var elasticsearchResult = objectMapper.readValue(responseBody, new TypeReference<ElasticsearchResult>() {});
            requestLog.setResultTook(elasticsearchResult.took);
            requestLog.setResultHitsTotal(Optional.ofNullable(elasticsearchResult.hits).map(o -> o.total).map(o -> o.value).orElse(null));
        } catch (JsonProcessingException e) {
            log.debug("Cannot parse elasticsearch result");
        }
        requestLogRepository.save(requestLog);
    }
}
