package com.main.eureka.api.controller;

import com.main.eureka.api.dto.RefreshToken;
import com.main.eureka.common.response.Response;
import com.main.eureka.domain.enums.OAuth2Provider;
import com.main.eureka.security.oauth2.impl.OAuth2ServiceImpl;
import com.main.eureka.security.oauth2.request.OAuth2CallbackRequest;
import com.main.eureka.security.oauth2.request.OAuth2UrlRequest;
import com.main.eureka.security.oauth2.response.OAuth2UrlResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "OAuth2 Management", description = "OAuth2 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth2")
public class ApiController {
    private final OAuth2ServiceImpl oAuth2Service;

    @Operation(summary = "제공되는 소셜 로그인 목록",
            description = "현재 제공되는 소셜 로그인 목록을 조회합니다. 개발자 확인용")
    @GetMapping("/support")
    public ResponseEntity<Response<OAuth2Provider[]>> connect() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();;
        String userId = auth.getName();

        System.out.println(userId);

        return ResponseEntity.ok(Response.payload(true,
                "200",
                oAuth2Service.getSupportedProviders(),
                "list to social logins"));
    }

    @Operation(summary = "소셜 로그인 인가 코드 요청",
            description = "kakao, naver 로그인 진행을 위한 인가 코드 요청")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리됨."),
                    @ApiResponse(responseCode = "500", description = "서버 내부에서 예상치 못한 오류 발생."),
                    @ApiResponse(responseCode = "502", description = "서버가 다른 서버로부터 올바른 응답을 받지 못함."),
                    @ApiResponse(responseCode = "503", description = "서비스 점검 중이거나 사용할 수 없음.")
            }
    )
    @PostMapping("/authorize")
    public ResponseEntity<Response<OAuth2UrlResponse>> authorize(@RequestBody OAuth2UrlRequest oAuth2UrlRequest) {
        return ResponseEntity.ok(Response.payload(true,
                "200",
                oAuth2Service.getAuthUrl(oAuth2UrlRequest),
                "authorize success"));
    }

    @Operation(summary = "로그인 - 리다이렉트 처리시 사용자 정보 요청",
            description = "리다이렉트로 처리하는 소셜 로그인의 경우 앱에서 요청을 받아 사용자 정보를 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리됨."),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 오류. 주로 API에 필요한 필수 파라미터와 관련된 오류입니다."),
                    @ApiResponse(responseCode = "401", description = "인증 오류"),
                    @ApiResponse(responseCode = "403", description = "권한/퍼미션 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부에서 예상치 못한 오류 발생.")
            }
    )
    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<?>> callback(@RequestBody OAuth2CallbackRequest request) {
        return ResponseEntity.ok(Response.payload(true,
                "200", oAuth2Service.getCallBack(request), "login success"));
    }

    @Operation(summary = "로그아웃 - 리프레시 토큰 무효화",
            description = "리프레시 토큰을 무효화 로그아웃 처리")
    @PostMapping(value = "/logout")
    public ResponseEntity<Response<Void>> logout(@RequestBody RefreshToken refreshToken) {
        oAuth2Service.logout(refreshToken);
        return ResponseEntity.ok(Response.payload(true, "200", "리프레시 토큰 무효화"));
    }

    @Operation(summary = "리프레시 토큰 갱신",
            description = "리프레시 토큰을 무효화 로그아웃 처리")
    @PostMapping(value = "/refresh")
    public ResponseEntity<Response<Void>> refresh(@RequestBody RefreshToken refreshToken) {
        oAuth2Service.logout(refreshToken);
        return ResponseEntity.ok(Response.payload(true, "200", "리프레시 토큰 갱신"));
    }

    /**
     * 리다이렉트 테스트용 api
     * */
    @Hidden
    @GetMapping("/redirect")
    public ResponseEntity<String> handleRedirect(@RequestParam String code) {
        return ResponseEntity.ok(code);
    }
}
