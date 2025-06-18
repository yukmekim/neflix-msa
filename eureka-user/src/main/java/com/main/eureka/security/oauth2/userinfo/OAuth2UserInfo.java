package com.main.eureka.security.oauth2.userinfo;

import com.main.eureka.domain.enums.OAuth2Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 소셜 로그인 성공시 제공받은 사용자 기본 정보를 매핑합니다.
 * */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
    private String providerId;
    private String email;
    private String name;
    private String profileImageUrl;
    private OAuth2Provider provider;
    private Map<String, Object> attributes;

    public static OAuth2UserInfo from(OAuth2Provider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case KAKAO -> fromKakao(attributes);
            case NAVER -> fromNaver(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 제공자입니다: " + provider);
        };
    }

    @SuppressWarnings("unchecked")
    private static OAuth2UserInfo fromKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2UserInfo.builder()
                .providerId(String.valueOf(attributes.get("id")))
                .email((String) kakaoAccount.get("email"))
                .name((String) profile.get("nickname"))
                .profileImageUrl((String) profile.get("profile_image_url"))
                .provider(OAuth2Provider.KAKAO)
                .attributes(attributes)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuth2UserInfo fromNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2UserInfo.builder()
                .providerId((String) response.get("id"))
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .profileImageUrl((String) response.get("profile_image"))
                .provider(OAuth2Provider.NAVER)
                .attributes(attributes)
                .build();
    }
}
