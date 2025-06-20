package com.main.eureka.api.controller;

import com.main.eureka.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "OAuth2 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class ApiController {

    @Operation(summary = "사용자 API 연결 상태 확인")
    @GetMapping("/connect")
    public ResponseEntity<Response<Void>> connect() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();;
        String userId = auth.getName();

        System.out.println(userId);

        return ResponseEntity.ok(Response.payload(true,"200","connect to user service"));
    }
}
