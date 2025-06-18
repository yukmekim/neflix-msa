package com.main.eureka.security.oauth2.response;

import com.main.eureka.api.dto.TokenResponse;
import com.main.eureka.domain.enums.OAuth2Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2LoginResponse {
    private Long userId;
    private String name;
    private String email;
    private String profileImageUrl;
    private OAuth2Provider provider;

    private TokenResponse tokens;

    private boolean isNewUser;
}
