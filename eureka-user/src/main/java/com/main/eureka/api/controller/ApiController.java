package com.main.eureka.api.controller;

import com.main.eureka.api.dto.Response;
import com.main.eureka.api.service.Oauth2Service;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "사용자 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class ApiController {
    private final Oauth2Service oAuth2Service;

    @Operation(summary = "사용자 API 연결 상태 확인")
    @GetMapping("/connect")
    public ResponseEntity<Response<Void>> connect() {
        return ResponseEntity.ok(Response.payload(true, "200", "connect to user client"));
    }

    /**
     * Kakao, Naver 로그인 진행시 인가 코드 요청
     * @param scope 추가 항목 동의 받기 요청 시 사용, 사용자에게 동의 요청할 동의항목 ID 목록
     * @param type 인가 요청할 소셜 타입
     * @return redirect url
     * */
    @Operation(summary = "소셜 로그인 인가 코드 요청",
            description = "Kakao, Naver 로그인 진행을 위한 인가 코드 요청")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리됨."),
                    @ApiResponse(responseCode = "500", description = "서버 내부에서 예상치 못한 오류 발생."),
                    @ApiResponse(responseCode = "502", description = "서버가 다른 서버로부터 올바른 응답을 받지 못함."),
                    @ApiResponse(responseCode = "503", description = "서비스 점검 중이거나 사용할 수 없음.")
            }
    )
    @GetMapping("/authorize")
    public ResponseEntity<Response<String>> authorize(@RequestParam(required = false) String scope,
                                                      @RequestParam String type) {
        return ResponseEntity.ok(Response.payload(true
                , "200"
                , oAuth2Service.getAuthUrl(scope, type)
                ,"authorize success"));
    }

    @Hidden
    @GetMapping("/redirect")
    public ResponseEntity<String> handleRedirect(@RequestParam String code) {
        return ResponseEntity.ok(code);
    }

    /**
     * redirect url을 flutter app으로 전달 - 개발 방향 맞는지 코디님 확인 필요
     * flutter에서 전달받은 code로 callback 요청
     * jwt 토큰 발급 및 사용자 정보 최소한 수집 및 자동 가입 진행
     * */
    @PostMapping("/callback")
    public ResponseEntity<Response<?>> handleCallback(@RequestBody String code) {
        return ResponseEntity.ok(oAuth2Service.getUserProfile(code));
    }
}
