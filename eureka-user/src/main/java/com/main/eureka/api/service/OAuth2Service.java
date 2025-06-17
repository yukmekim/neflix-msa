package com.main.eureka.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.eureka.api.dto.KaKaoToken;
import com.main.eureka.common.response.Response;
import com.main.eureka.api.dto.OAuthRequest;
import com.main.eureka.api.dto.UserResponse;
import com.main.eureka.common.util.HttpUtil;
import com.main.eureka.domain.repository.RefreshTokenRepository;
import com.main.eureka.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {
    private final KakaoLoginService kakaoLoginService;

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public String getAuthUrl(String scope, String type) {
        return kakaoLoginService.getAuthUrl(scope);
    }

    public String getAccessToken(String code) {
        try {
            String response = HttpUtil.postForm(kakaoLoginService.getTokenUrl(), kakaoLoginService.getTokenParams(code));
            KaKaoToken tokenInfo = objectMapper.readValue(response, KaKaoToken.class);

            return tokenInfo.getAccess_token();
        } catch (Exception e) {
            log.error("AccessToken error : {}", e.getMessage());
            return null;
        }
    }

    public Response<?> getUserProfile(OAuthRequest request) {
        try {
            String accessToken = Optional.ofNullable(getAccessToken(request.getCode()))
                    .orElseThrow(() -> new BadCredentialsException("invalid_grant : authorization code not found"));

            String response = HttpUtil.postForm(kakaoLoginService.getProfileUrl(), accessToken, null);

            //TODO 소셜 로그인별 사용자 정보(profileNode) 파싱 필요
            JsonNode profileNode = objectMapper.readTree(response);
            String userId = profileNode.path("id").asText();
            String userEmail = profileNode.path("kakao_account").path("email").asText();
            String userName = profileNode.path("properties").path("nickname").asText();

            // TODO 데이터베이스에 가입 정보 확인후, 가입 또는 회원 정보 조회 로직 추가 - 소셜 로그인에서 제공하는 기본 정보만 저장

            String jwtToken = jwtTokenProvider.generateAccessToken(userId, userEmail, userName);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

            // TODO Token 관리 Redis ttl 설정 key value 관리 (userId - refreshToken? or userId + JTI - refreshToken? or .. )
            refreshTokenRepository.save(userId, refreshToken);

            UserResponse userResponse = UserResponse.builder()
                    .userId(userId)
                    .accessToken(jwtToken)
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();

            return Response.payload(true, "200", userResponse, "profile");
        } catch (BadCredentialsException bce) {
            log.error(bce.getMessage());
            return Response.payload(false, "401", bce.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.payload(false, "500", "서버 내부에서 예상치 못한 오류 발생.");
        }
    }
}
