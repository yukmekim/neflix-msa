package com.main.eureka.domain.enums;

import lombok.Getter;

import java.util.Arrays;

 // application 설정에 추가한 registration 서치를 위한 enum
@Getter
public enum OAuth2Provider {
    KAKAO("kakao"),
    NAVER("naver");
    //APPLE("apple");

    private final String registrationId;
    OAuth2Provider(String registrationId) {
        this.registrationId = registrationId;
    }

    public static OAuth2Provider fromRegistrationId(String registrationId) {
        return Arrays.stream(values())
                .filter(provider -> provider.registrationId.equals(registrationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + registrationId));
    }
}
