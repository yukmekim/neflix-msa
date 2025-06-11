package com.main.eureka.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // JWT 생성 메서드
    public String generateToken(String userId, String userEmail, String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userEmail);
        claims.put("name", userName);
        // 필요하다면 사용자 권한(roles) 등 추가 정보 클레임에 추가

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder().claims(claims)
                .subject(userId)
                .issuedAt(now)
                .expiration(expiryDate) // 만료 시간
                .signWith(getSigningKey())
                .compact();
    }

    // JWT에서 클레임 추출 메서드 (선택 사항, 서버에서 토큰 검증 시 사용)
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build().parseSignedClaims(token).getPayload();
    }

    // JWT 유효성 검증 메서드 (선택 사항, 서버에서 토큰 검증 시 사용)
    public Boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // 토큰 파싱 실패 (만료, 변조 등)
            return false;
        }
    }
}
