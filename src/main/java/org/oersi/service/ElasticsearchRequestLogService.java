package org.oersi.service;

public interface ElasticsearchRequestLogService {

    void logRequest(String body, String method, String path, String urlRequestQueryString, String responseBody, String userAgent, String referer);
}
