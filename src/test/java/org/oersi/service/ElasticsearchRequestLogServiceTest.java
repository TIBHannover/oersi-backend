package org.oersi.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oersi.ElasticsearchServicesMock;
import org.oersi.domain.ElasticsearchRequestLog;
import org.oersi.repository.ElasticsearchRequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(ElasticsearchServicesMock.class)
class ElasticsearchRequestLogServiceTest {

    @Autowired
    private ElasticsearchRequestLogService service;
    @Autowired
    private ElasticsearchRequestLogRepository requestLogRepository; // mock from ElasticsearchServicesMock

    @Test
    void testLogRequest() {
        service.logRequest("{\"test\": \"test\"}", "POST", "testindex/_search", "{\"took\": 3, \"hits\": {\"total\": {\"value\": 513}}}");

        ArgumentCaptor<ElasticsearchRequestLog> argumentCaptor = ArgumentCaptor.forClass(ElasticsearchRequestLog.class);
        Mockito.verify(requestLogRepository, Mockito.timeout(100)).save(argumentCaptor.capture());
        ElasticsearchRequestLog result = argumentCaptor.getValue();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("POST", result.getMethod());
        Assertions.assertEquals(3, result.getResultTook());
        Assertions.assertEquals(513, result.getResultHitsTotal());
    }

    @Test
    void testLogRequestWithoutResult() {
        service.logRequest("{\"test\": \"test\"}", "POST", "testindex/_search", "cannot parse result");

        ArgumentCaptor<ElasticsearchRequestLog> argumentCaptor = ArgumentCaptor.forClass(ElasticsearchRequestLog.class);
        Mockito.verify(requestLogRepository, Mockito.timeout(100)).save(argumentCaptor.capture());
        ElasticsearchRequestLog result = argumentCaptor.getValue();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("POST", result.getMethod());
        Assertions.assertNull(result.getResultTook());
        Assertions.assertNull(result.getResultHitsTotal());
    }
}
