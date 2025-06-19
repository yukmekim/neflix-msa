package com.main.eureka.security.oauth2.impl;

import com.main.eureka.api.dto.RefreshToken;
import com.main.eureka.api.dto.TokenResponse;
import com.main.eureka.domain.enums.OAuth2Provider;
import com.main.eureka.domain.repository.RefreshTokenRepository;
import com.main.eureka.security.jwt.JwtTokenProvider;
import com.main.eureka.security.oauth2.OAuth2Service;
import com.main.eureka.security.oauth2.request.OAuth2CallbackRequest;
import com.main.eureka.security.oauth2.request.OAuth2UrlRequest;
import com.main.eureka.security.oauth2.response.OAuth2LoginResponse;
import com.main.eureka.security.oauth2.response.OAuth2UrlResponse;
import com.main.eureka.security.oauth2.userinfo.OAuth2UserInfo;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2ServiceImpl implements OAuth2Service {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    // JWT 토큰 제공 클래스
    private final JwtTokenProvider jwtTokenProvider;

    // RedisTemplate RefreshToken 관리
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 준비된 소셜 로그인 페이지를 전달
     * @param request 로그인 페이지 인가를 위한 요청 값
     * @return oAuth2UrlResponse 소셜 로그인 페이지를 리턴합니다.
     * */
    @Override
    public OAuth2UrlResponse getAuthUrl(OAuth2UrlRequest request) {
        try {
            OAuth2Provider oAuth2Provider = OAuth2Provider.fromRegistrationId(request.getProvider());
            ClientRegistration clientRegistration = getClientRegistration(oAuth2Provider);

            String state = StringUtils.hasText(request.getState()) ?
                    request.getState() : generateState();

            Set<String> scopes = parseScopes(request.getScope(), clientRegistration);
            String redirectUri = StringUtils.hasText(request.getRedirectUri()) ?
                    request.getRedirectUri() : clientRegistration.getRedirectUri();

            OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest
                    .authorizationCode()
                    .clientId(clientRegistration.getClientId())
                    .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                    .redirectUri(redirectUri)
                    .scopes(scopes)
                    .state(state)
                    .build();

            String authUrl = authorizationRequest.getAuthorizationRequestUri();

            return OAuth2UrlResponse.builder()
                    .authorizationUrl(authUrl)
                    .provider(oAuth2Provider)
                    .build();
        } catch (Exception e) {
            log.error("OAuth2 인증 URL 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 로그인 성공 결과에 따른 콜백 요청을 처리
     * @param request 콜백 요청 전달 값
     * @return OAuth2LoginResponse JWT 토큰값을 포함한 로그인 응답값을 리턴합니다.
     * */
    @Override
    public OAuth2LoginResponse getCallBack(OAuth2CallbackRequest request) {
        try {
            OAuth2Provider provider = OAuth2Provider.fromRegistrationId(request.getProvider());
            ClientRegistration clientRegistration = getClientRegistration(provider);

            // Access Token 획득
            OAuth2AccessTokenResponse tokenResponse = getAccessToken(clientRegistration, request);

            // 사용자 정보 획득후 사용자 정보 dto 매핑
            OAuth2UserInfo userInfo = getUserInfo(provider, tokenResponse.getAccessToken().getTokenValue());

            // TODO 스키마 설계 이후, RDBMS 사용자 조회 또는 생성
            //User user = userService.findOrCreateOAuth2User(userInfo);
            //boolean isNewUser = user.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5));

            // JWT 토큰 생성
            TokenResponse tokens = createTokens(userInfo);

            return OAuth2LoginResponse.builder()
                    .name(userInfo.getName())
                    .email(userInfo.getEmail())
                    .profileImageUrl(userInfo.getProfileImageUrl())
                    .provider(provider)
                    .tokens(tokens)
                    // .isNewUser(isNewUser)
                    .build();

        } catch (Exception e) {
            log.error("OAuth2 콜백 처리 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 전달 받은 사용자 정보를 DTO 에 매핑
     * @param provider 제공자 정보 enums
     * @param accessToken 소셜 로그인에서 발급 받은 accessToken
     * @return OAuth2UserInfo 사용자 정보를 리턴합니다.
     * */
    @Override
    public OAuth2UserInfo getUserInfo(OAuth2Provider provider, String accessToken) {
        try {
            ClientRegistration clientRegistration = getClientRegistration(provider);

            OAuth2UserRequest userRequest = new OAuth2UserRequest(
                    clientRegistration,
                    new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, accessToken, null, null)
            );

            OAuth2User oauth2User = oAuth2UserService.loadUser(userRequest);

            return OAuth2UserInfo.from(provider, oauth2User.getAttributes());
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 현재 제공되는 소셜 로그인 목록을 조회 (개발자 확인용)
     * @return oAuth2Provider[] 소셜 로그인 제공 목록을 리턴합니다.
     * */
    @Override
    public OAuth2Provider[] getSupportedProviders() {
        return OAuth2Provider.values();
    }

    /**
     * 리프레시 토큰을 무효화하여 로그아웃 치리
     *
     * @param refreshToken 리프레시 토큰 정보
     */
    @Override
    public void logout(RefreshToken refreshToken) {
        try {
            String token = refreshToken.getRefreshToken();
            // 요효한 토큰인지 우선 검증
            if(jwtTokenProvider.validateToken(token)) {
                refreshTokenRepository.delete(token);
            } else {
                throw new UnsupportedJwtException("유효하지 않은 토큰 정보입니다.");
            }
        } catch (UnsupportedJwtException uje) {
            log.error("UnsupportedJwtException : {}", uje.getMessage());
        }
    }

    /**
     * 소셜 AccessToken 발급
     * @param clientRegistration oauth2 설정 registration 값
     * @param request Callback 시 전달 받는 값
     * @return OAuth2AccessTokenResponse 토큰 정보를 리턴합니다.
     * */
    private OAuth2AccessTokenResponse getAccessToken(ClientRegistration clientRegistration,
                                                     OAuth2CallbackRequest request) {
        String redirectUri = clientRegistration.getRedirectUri();

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(clientRegistration.getClientId())
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .redirectUri(redirectUri)
                .state(request.getState())
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse
                .success(request.getCode())
                .redirectUri(redirectUri)
                .state(request.getState())
                .build();

        OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(
                clientRegistration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
        );

        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient =
                new RestClientAuthorizationCodeTokenResponseClient();

        return tokenResponseClient.getTokenResponse(grantRequest);
    }

    /**
     * application.yml 파일에 설정해준 registration 정보를 서치
     * @param provider 소셜 로그인 제공자 enum 상수
     * @return clientRegistration 해당되는 제공자의 registration 정보를 리턴합니다.
     * */
    private ClientRegistration getClientRegistration(OAuth2Provider provider) {
        ClientRegistration clientRegistration = clientRegistrationRepository
                .findByRegistrationId(provider.getRegistrationId());

        Optional.ofNullable(clientRegistration)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("지원하지 않는 OAuth2 제공자입니다: %s", provider.getRegistrationId())));

        return clientRegistration;
    }

    /**
     * 로그인한 사용자에게 JWT 토큰 발급
     * @param user 사용자 정보
     * @return TokenResponse 토큰 정보를 리턴합니다.
     * */
    private TokenResponse createTokens(OAuth2UserInfo user) {
        // TODO Claim 사용 userId 값 스키마 설계후 RDBMS 에서 생성한 USER PK 값으로 변경 필요
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getProviderId(), user.getEmail(), user.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getProviderId());

        // Refresh Token 저장 - Redis 관리
        refreshTokenRepository.save(user.getProviderId(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 동의 항목 요청 값들
     * 여러 개 요청시 중복 없는 리스트로 파싱
     * @param scope 동의 항목 (,)로 구분
     * @param clientRegistration oauth2 설정 registration 값
     * @return clientRegistration.getScopes() 중복 없는 문자열 리스트를 리턴합니다.
     * */
    private Set<String> parseScopes(String scope, ClientRegistration clientRegistration) {
        if (StringUtils.hasText(scope)) {
            return new HashSet<>(Arrays.asList(scope.split(",")));
        }
        return clientRegistration.getScopes();
    }

    /**
     * 네이버 로그인의 경우 랜덤 state 코드가 필요함
     * */
    private String generateState() {
        return UUID.randomUUID().toString();
    }
}
