package com.main.eureka.security.oauth2.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OAuth2UrlRequest {
    @NotBlank(message = "제공자는 필수!")
    @Pattern(regexp = "^(kakao|naver)$", message = "지원하지 않는 제공자입니다") // kakao|naver|apple 추후 추가 예정
    private String provider;
    private String state;
    private String scope;

    // 테스트 환경에 따른 redirectUri 요청 값
    private String redirectUri;
}
