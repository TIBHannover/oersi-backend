package org.oersi.service;

public interface ElasticsearchRequestLogService {

    void logRequest(String body, String method, String path, String responseBody);
}
