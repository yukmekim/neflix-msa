package com.main.eureka.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request); // HTTP 요청에서 JWT 토큰 추출

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) { // 토큰이 유효한지 검증
                // 토큰에서 사용자 ID/클레임 추출
                String userId = jwtTokenProvider.extractAllClaims(jwt).getSubject(); // subject = userId

                // 인증 객체 생성
                // 실제 애플리케이션에서는 userId를 통해 DB에서 사용자 정보를 가져와 GrantedAuthority를 설정합니다.
                // 여기서는 간단하게 USER 권한을 부여합니다.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList() // 사용자의 권한 (GrantedAuthority) 리스트
                );

                // SecurityContext에 인증 객체 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // JWT 유효성 검증 실패 시 로그 기록
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response); // 다음 필터로 요청 전달
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // "Bearer " 접두사로 시작하는 JWT 토큰 추출
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}