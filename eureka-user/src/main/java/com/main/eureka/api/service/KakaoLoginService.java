package com.main.eureka.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.eureka.api.dto.KaKaoToken;
import com.main.eureka.api.dto.UserProfile;
import com.main.eureka.common.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginService implements OAuth2LoginService {

    @Value("${sso.kakao.host}")
    private String authHost;

    @Value("${sso.kakao.api-host}")
    private String apiHost;

    @Value("${sso.kakao.client-id}")
    private String clientId;

    @Value("${sso.kakao.client-secret}")
    private String clientSecret;

    @Value("${sso.kakao.redirect-uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper;

    private final static String ACCESS_TOKEN_URL = "/oauth/token";
    private final static String USER_PROFILE_URL = "/v2/user/me";

    @Override
    public String getAuthUrl(String scope) {
        return UriComponentsBuilder.fromUriString(authHost + "/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParamIfPresent("scope",  scope != null ? Optional.of(scope) : Optional.empty())
                .build()
                .toUriString();
    }

    @Override
    public UserProfile getUserProfile(String code) {
        try {
            String accessToken = Optional.ofNullable(getAccessToken(code))
                    .orElseThrow(() -> new BadCredentialsException("invalid_grant : authorization code not found"));

            String response = HttpUtil.postForm(apiHost + USER_PROFILE_URL, accessToken, null);

            //TODO 소셜 로그인별 사용자 정보(profileNode) 파싱 필요
            JsonNode profileNode = objectMapper.readTree(response);
            String userId = profileNode.path("id").asText();
            String userEmail = profileNode.path("kakao_account").path("email").asText();
            String userName = profileNode.path("properties").path("nickname").asText();

            return UserProfile.builder()
                    .userId(userId)
                    .email(userEmail)
                    .name(userName)
                    .build();
        } catch (BadCredentialsException bce) {
            log.error("BadCredentialsException : {}",bce.getMessage());
            return null;
        } catch (Exception e) {
            log.error("User Profile Error : {}", e.getMessage());
            return null;
        }
    }

    private MultiValueMap<String,String> getTokenParams(String code) {
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);

        return params;
    }

    private String getAccessToken(String code) {
        try {
            String response = HttpUtil.postForm(authHost + ACCESS_TOKEN_URL, getTokenParams(code));
            KaKaoToken tokenInfo = objectMapper.readValue(response, KaKaoToken.class);

            return tokenInfo.getAccess_token();
        } catch (Exception e) {
            log.error("AccessToken error : {}", e.getMessage());
            return null;
        }
    }
}
