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
import org.springframework.stereotype.Service;

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
                private int value;
            }
            private ElasticsearchResultHitsTotal total;
        }
        private long took;
        private ElasticsearchResultHits hits;
    }

    private final @NonNull ElasticsearchRequestLogRepository requestLogRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void logRequest(String body, String method, String path, String responseBody) {
        ElasticsearchRequestLog requestLog = new ElasticsearchRequestLog();
        requestLog.setMethod(method);
        requestLog.setPath(path);
        requestLog.setBody(body);
        try {
            var elasticsearchResult = objectMapper.readValue(responseBody, new TypeReference<ElasticsearchResult>() {});
            requestLog.setResultTook(elasticsearchResult.took);
            requestLog.setResultHitsTotal(elasticsearchResult.hits.total.value);
        } catch (JsonProcessingException e) {
            log.debug("Cannot parse elasticsearch result");
        }
        requestLogRepository.save(requestLog);
    }
}
