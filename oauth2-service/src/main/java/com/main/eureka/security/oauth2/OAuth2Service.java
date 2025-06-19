package com.main.eureka.security.oauth2;

import com.main.eureka.domain.enums.OAuth2Provider;
import com.main.eureka.security.oauth2.request.OAuth2CallbackRequest;
import com.main.eureka.security.oauth2.request.OAuth2UrlRequest;
import com.main.eureka.security.oauth2.response.OAuth2LoginResponse;
import com.main.eureka.security.oauth2.response.OAuth2UrlResponse;
import com.main.eureka.security.oauth2.userinfo.OAuth2UserInfo;

public interface OAuth2Service {
    OAuth2UrlResponse getAuthUrl(OAuth2UrlRequest request);

    OAuth2Provider[] getSupportedProviders();

    OAuth2UserInfo getUserInfo(OAuth2Provider provider, String accessToken);

    OAuth2LoginResponse getCallBack(OAuth2CallbackRequest request);
}
