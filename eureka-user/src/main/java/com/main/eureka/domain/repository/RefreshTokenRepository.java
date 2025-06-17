package com.main.eureka.domain.repository;

import com.main.eureka.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String TOKEN_KEY = "token:";

    public void save(String userId, String token) {
        String key = String.format("%s:%s:%s",TOKEN_KEY, userId, jwtTokenProvider.extractAllClaims(token).getId());
        long expiration = jwtTokenProvider.extractAllClaims(token).getExpiration().getTime() - System.currentTimeMillis();

        redisTemplate.opsForValue().set(key,
                token,
                expiration, TimeUnit.MILLISECONDS);
    }
}
