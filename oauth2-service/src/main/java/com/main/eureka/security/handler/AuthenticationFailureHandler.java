package com.main.eureka.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.eureka.common.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFailureHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {
        response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // TODO 추가 예외 처리 구현 필요!
        Response<Void> authResponse = Response.payload(false, "401", "로그인후 이용 가능한 서비스 입니다.");

        // JSON 형태로 응답 작성
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(authResponse));
    }
}
