package com.main.eureka.security.oauth2.request;

import lombok.Data;

@Data
public class OAuth2CallbackRequest {
    private String provider;
    private String redirectUri;
    private String state;
    private String code;
}
