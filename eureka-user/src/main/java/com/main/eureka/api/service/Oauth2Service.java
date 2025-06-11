package com.main.eureka.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.eureka.api.dto.KaKaoToken;
import com.main.eureka.api.dto.Response;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class Oauth2Service {
    private final KakaoLoginService kakaoLoginService;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.builder().build();

    public String getAuthUrl(String scope, String type) {
        return kakaoLoginService.getAuthUrl(scope);
    }

    public HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession();
    }

    public void getAccessToken(String code) {
        try {
            String response = httpRequest("POST"
                    , kakaoLoginService.getTokenUrl()
                    , kakaoLoginService.getTokenParams(code));
            KaKaoToken tokenInfo = objectMapper.readValue(response, KaKaoToken.class);

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            getSession().setAttribute("access_token", tokenInfo.getAccess_token());
        } catch (Exception e) {
            log.error("AccessToken error : {}", e.getMessage());
        }
    }

    public Response<?> getUserProfile(String code) {
        try {
            getAccessToken(code);
            String response = httpRequest( "POST", kakaoLoginService.getProfileUrl(), null);
            return Response.payload(true, "200", objectMapper.readValue(response, Object.class), "profile");
        } catch (Exception e) {
            return Response.payload(false, "500", "서버 오류");
        }
    }

    public String httpRequest(String method, String url, String body) {
        String accessToken = String.valueOf(getSession().getAttribute("access_token"));
        RestClient.RequestBodySpec requestBodySpec = restClient.method(HttpMethod.valueOf(method))
                .uri(url)
                .headers(headers -> headers.setBearerAuth(accessToken));
        if (body != null) {
            requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body);
        }
        try {
            return requestBodySpec.retrieve()
                    .body(String.class);
        } catch (RestClientResponseException rcre) {
            log.error("RestClient error : {}", rcre.getResponseBodyAsString());
            return rcre.getResponseBodyAsString();
        }
    }
}
