package com.main.eureka.security.oauth2.response;

import com.main.eureka.domain.enums.OAuth2Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UrlResponse {
    private String authorizationUrl;
    private String state;
    private OAuth2Provider provider;
}
