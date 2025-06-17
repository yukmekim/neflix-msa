package com.main.eureka.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class KakaoLoginService {

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

    public String getAuthUrl(String scope) {
        return UriComponentsBuilder.fromUriString(authHost + "/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParamIfPresent("scope",  scope != null ? Optional.of(scope) : Optional.empty())
                .build()
                .toUriString();
    }

    public MultiValueMap<String,String> getTokenParams(String code) {
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);

        return params;
    }

    public String getTokenUrl() {
        return authHost + "/oauth/token";
    }

    public String getProfileUrl() {
        return apiHost + "/v2/user/me";
    }
}
