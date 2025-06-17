package com.main.eureka.api.service;

import com.main.eureka.api.dto.UserProfile;
import com.main.eureka.common.response.Response;
import com.main.eureka.api.dto.OAuthRequest;
import com.main.eureka.api.dto.UserResponse;
import com.main.eureka.domain.repository.RefreshTokenRepository;
import com.main.eureka.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {
    private final KakaoLoginService kakaoLoginService;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private final Map<String, OAuth2LoginService> services;

    public Response<String> getAuthUrl(String provider, String scope) {
        try {
            OAuth2LoginService service = Optional.ofNullable(services.get(provider + "LoginService"))
                    .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다."));
            return Response.payload(true, "200", service.getAuthUrl(scope), "authorize success");
        } catch (IllegalArgumentException iae) {
            log.error("IllegalArgumentException : {}", iae.getMessage());
            return Response.payload(false, "400", iae.getMessage());
        }
    }

    public Response<?> getUserProfile(OAuthRequest request) {
        try {
            OAuth2LoginService service = Optional.ofNullable(services.get(request.getProvider() + "LoginService"))
                    .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다."));

            UserProfile userProfile = service.getUserProfile(request.getCode());
            String userId = userProfile.getUserId();
            String userEmail = userProfile.getEmail();
            String userName = userProfile.getName();

            // TODO 데이터베이스에 가입 정보 확인후, 가입 또는 회원 정보 조회 로직 추가 - 소셜 로그인에서 제공하는 기본 정보만 저장

            String jwtToken = jwtTokenProvider.generateAccessToken(userId, userEmail, userName);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

            // TODO Token 관리 Redis ttl 설정 key value 관리 (userId - refreshToken? or userId + JTI - refreshToken? or .. )
            refreshTokenRepository.save(userId, refreshToken);

            UserResponse userResponse = UserResponse.builder()
                    .userId(userId)
                    .name(userName)
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();

            return Response.payload(true, "200", userResponse, "login success");
        } catch (IllegalArgumentException iae) {
            log.error("IllegalArgumentException : {}", iae.getMessage());
            return Response.payload(false, "400", iae.getMessage());
        } catch (BadCredentialsException bce) {
            log.error("BadCredentialsException : {}", bce.getMessage());
            return Response.payload(false, "401", bce.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.payload(false, "500", "서버 내부에서 예상치 못한 오류 발생.");
        }
    }
}
