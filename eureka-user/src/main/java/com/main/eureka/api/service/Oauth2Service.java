package com.main.eureka.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.eureka.api.dto.KaKaoToken;
import com.main.eureka.api.dto.Response;
import com.main.eureka.api.dto.UserRequest;
import com.main.eureka.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Oauth2Service {
    private final KakaoLoginService kakaoLoginService;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.builder().build();
    private final JwtTokenProvider jwtTokenProvider;

    public String getAuthUrl(String scope, String type) {
        return kakaoLoginService.getAuthUrl(scope);
    }

    public String getAccessToken(String code) {
        try {
            String response = httpRequest("POST"
                    , kakaoLoginService.getTokenUrl()
                    , kakaoLoginService.getTokenParams(code));
            KaKaoToken tokenInfo = objectMapper.readValue(response, KaKaoToken.class);

            return tokenInfo.getAccess_token();
        } catch (Exception e) {
            log.error("AccessToken error : {}", e.getMessage());
            return null;
        }
    }

    public Response<?> getUserProfile(UserRequest request) {
        try {
            String accessToken = Optional.ofNullable(getAccessToken(request.getCode()))
                    .orElseThrow(() -> new BadCredentialsException("invalid_grant : authorization code not found"));

            String response = httpRequest( "POST", kakaoLoginService.getProfileUrl(), accessToken, null);

            JsonNode profileNode = objectMapper.readTree(response);
            String userId = profileNode.path("id").asText();
            String userEmail = profileNode.path("kakao_account").path("email").asText();
            String userName = profileNode.path("properties").path("nickname").asText();

            // TODO 데이터베이스에 가입 정보 확인후, 가입 또는 회원 정보 조회 로직 추가 - 소셜 로그인에서 제공하는 기본 정보만 저장

            // TODO Token 관리 RDBMS, Redis 중 고민 필요
            String jwtToken = jwtTokenProvider.generateAccessToken(userId, userEmail, userName);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

            Map<String, Object> loginInfo = new HashMap<>();
            loginInfo.put("accessToken", jwtToken);
            loginInfo.put("refreshToken", refreshToken);
            loginInfo.put("userProfile", profileNode);

            return Response.payload(true, "200", loginInfo, "profile");
        } catch (BadCredentialsException bce) {
            log.error(bce.getMessage());
            return Response.payload(false, "401", bce.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.payload(false, "500", "서버 내부에서 예상치 못한 오류 발생.");
        }
    }

    public String httpRequest(String method, String url, String body) {
        RestClient.RequestBodySpec requestBodySpec = restClient.method(HttpMethod.valueOf(method))
                .uri(url);
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

    public String httpRequest(String method, String url, String token, String body) {
        RestClient.RequestBodySpec requestBodySpec = restClient.method(HttpMethod.valueOf(method))
                .uri(url)
                .headers(headers -> headers.setBearerAuth(token));
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
