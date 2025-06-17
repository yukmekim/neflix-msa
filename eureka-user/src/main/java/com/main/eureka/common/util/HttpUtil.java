package com.main.eureka.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RequiredArgsConstructor
public class HttpUtil {
    private static final RestClient restClient = RestClient.builder().build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static String execute(HttpMethod method, String url, HttpHeaders headers, Object body, MediaType contentType) {
        RestClient.RequestBodySpec requestSpec = restClient.method(method).uri(url);

        if (headers != null) {
            requestSpec = requestSpec.headers(h -> h.addAll(headers));
        }

        if (body != null) {
            requestSpec = requestSpec.contentType(contentType);
            try {
                if (contentType == MediaType.APPLICATION_JSON) {
                    requestSpec = requestSpec.body(objectMapper.writeValueAsString(body));
                } else if (contentType == MediaType.APPLICATION_FORM_URLENCODED && body instanceof MultiValueMap) {
                    requestSpec = requestSpec.body(body);
                } else if (body instanceof String) {
                    requestSpec = requestSpec.body(body);
                } else {
                    log.warn("Unsupported body type {} for Content-Type {}. Body not attached for URL: {}", body.getClass().getName(), contentType, url);
                    return null;
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to convert body to JSON for URL: {}, Error: {}", url, e.getMessage(), e);
                return null;
            }
        }

        try {
            return requestSpec.retrieve().body(String.class);
        } catch (RestClientResponseException rcre) {
            log.error("RestClient error for {} {}: Status {}, Body: {}", method, url, rcre.getStatusCode(), rcre.getResponseBodyAsString(), rcre);
            return rcre.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Unexpected error during {} {}: {}", method, url, e.getMessage(), e);
            return null;
        }
    }

    public static String get(String url) {
        return execute(HttpMethod.GET, url, null, null, null);
    }

    public static String get(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // Authorization: Bearer {token} 헤더 설정
        return execute(HttpMethod.GET, url, headers, null, null);
    }

    public static String postJson(String url, Object body) {
        return execute(HttpMethod.POST, url, null, body, MediaType.APPLICATION_JSON);
    }

    public static String postJson(String url, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return execute(HttpMethod.POST, url, headers, body, MediaType.APPLICATION_JSON);
    }

    public static String postForm(String url, MultiValueMap<String, String> formData) {
        return execute(HttpMethod.POST, url, null, formData, MediaType.APPLICATION_FORM_URLENCODED);
    }

    public static String postForm(String url, String token, MultiValueMap<String, String> formData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return execute(HttpMethod.POST, url, headers, formData, MediaType.APPLICATION_FORM_URLENCODED);
    }
}
