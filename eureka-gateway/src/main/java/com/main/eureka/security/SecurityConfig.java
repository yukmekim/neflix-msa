package com.main.eureka.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                                .pathMatchers("/v3/api-docs/**", "/v3/webjars/swagger-ui/**", "/v3/swagger-ui.html").permitAll()
                                .pathMatchers("/","/api/v1/**").permitAll()
                                .anyExchange().authenticated()
                );

        return http.build();
    }
}
