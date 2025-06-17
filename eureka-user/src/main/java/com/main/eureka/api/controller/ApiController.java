package com.main.eureka.api.controller;

import com.main.eureka.api.dto.RefreshToken;
import com.main.eureka.common.response.Response;
import com.main.eureka.api.dto.OAuthRequest;
import com.main.eureka.api.service.OAuth2Service;
import com.main.eureka.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "사용자 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class ApiController {
    private final OAuth2Service oAuth2Service;

    @Operation(summary = "사용자 API 연결 상태 확인")
    @GetMapping("/connect")
    public ResponseEntity<Response<Void>> connect() {
        return ResponseEntity.ok(Response.payload(true, "200", "connect to user client"));
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
    @GetMapping("/authorize")
    public ResponseEntity<Response<String>> authorize(@RequestParam String provider,
                                                      @RequestParam(required = false) String scope) {
        return ResponseEntity.ok(oAuth2Service.getAuthUrl(provider, scope));
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
    @PostMapping(value = "/login-redirect", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<?>> loginRedirectRequest(@RequestBody OAuthRequest oAuthRequest) {
        return ResponseEntity.ok(oAuth2Service.getUserProfile(oAuthRequest));
    }

    @Operation(summary = "로그아웃 - 리프레시 토큰 무효화",
            description = "리프레시 토큰을 무효화 로그아웃 처리")
    @PostMapping(value = "/logout")
    public ResponseEntity<Response<Void>> logout(@RequestBody RefreshToken refreshToken) {
        return ResponseEntity.ok(Response.payload(true, "200", "리프레시 토큰 무효화"));
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
