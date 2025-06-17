package com.main.eureka.api.service;

import com.main.eureka.api.dto.UserProfile;
import com.main.eureka.domain.enums.OAuth2Provider;

public interface OAuth2LoginService {
    String getAuthUrl(String scope);
    UserProfile getUserProfile(String code) throws Exception;
}
