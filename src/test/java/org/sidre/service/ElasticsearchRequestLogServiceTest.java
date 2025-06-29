package org.sidre.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sidre.ElasticsearchServicesMock;
import org.sidre.domain.ElasticsearchRequestLog;
import org.sidre.repository.ElasticsearchRequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ElasticsearchServicesMock
class ElasticsearchRequestLogServiceTest {

    @Autowired
    private ElasticsearchRequestLogService service;
    @Autowired
    private ElasticsearchRequestLogRepository requestLogRepository; // mock from ElasticsearchServicesMock

    @Test
    void testLogRequest() {
        service.logRequest("{\"test\": \"test\"}", "POST", "testindex/_search", null, "{\"took\": 3, \"hits\": {\"total\": {\"value\": 513}}}", "useragent xxx", null);

        ArgumentCaptor<ElasticsearchRequestLog> argumentCaptor = ArgumentCaptor.forClass(ElasticsearchRequestLog.class);
        Mockito.verify(requestLogRepository, Mockito.timeout(500)).save(argumentCaptor.capture());
        ElasticsearchRequestLog result = argumentCaptor.getValue();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("POST", result.getMethod());
        Assertions.assertEquals(3, result.getResultTook());
        Assertions.assertEquals(513, result.getResultHitsTotal());
    }

    @Test
    void testLogRequestWithoutResult() {
        service.logRequest("{\"test\": \"test\"}", "POST", "testindex/_search", null, "cannot parse result", "useragent xxx", null);
        assertLoggedPostRequestWithoutLoggedResultValues();
    }

    @Test
    void testLogRequestWithResultWithoutTotal() {
        service.logRequest("{\"test\": \"test\"}", "POST", "testindex/_search", null, "{\"test\": \"test\"}", "useragent xxx", null);
        assertLoggedPostRequestWithoutLoggedResultValues();
    }

    @Test
    void testLogMultiSearchRequest() {
        service.logRequest("{}\n{\"test\": \"test\"}", "POST", "testindex/_msearch", null, "{\"responses\": [{\"took\": 3, \"hits\": {\"total\": {\"value\": 513}}}]}", "useragent xxx", null);

        ArgumentCaptor<ElasticsearchRequestLog> argumentCaptor = ArgumentCaptor.forClass(ElasticsearchRequestLog.class);
        Mockito.verify(requestLogRepository, Mockito.timeout(500)).save(argumentCaptor.capture());
        ElasticsearchRequestLog result = argumentCaptor.getValue();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("POST", result.getMethod());
        Assertions.assertEquals(3, result.getResultTook());
        Assertions.assertEquals(513, result.getResultHitsTotal());
    }

    @Test
    void testLogMultiSearchRequestWithoutResult() {
        service.logRequest("{}\n{\"test\": \"test\"}", "POST", "testindex/_msearch", null, "cannot parse result", "useragent xxx", null);
        assertLoggedPostRequestWithoutLoggedResultValues();
    }

    private void assertLoggedPostRequestWithoutLoggedResultValues() {
        ArgumentCaptor<ElasticsearchRequestLog> argumentCaptor = ArgumentCaptor.forClass(ElasticsearchRequestLog.class);
        Mockito.verify(requestLogRepository, Mockito.timeout(500)).save(argumentCaptor.capture());
        ElasticsearchRequestLog result = argumentCaptor.getValue();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("POST", result.getMethod());
        Assertions.assertNull(result.getResultTook());
        Assertions.assertNull(result.getResultHitsTotal());
    }
}
