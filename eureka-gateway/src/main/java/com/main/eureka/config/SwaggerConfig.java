package com.main.eureka.config;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Eureka에 등록된 API를 동적으로 Swagger UI에 추가해주는 Bean
     * @param discoveryClient Eureka 서비스 탐색을 위한 클라이언트
     * @param swaggerUiConfigParameters Swagger UI 설정 파라미터
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner swaggerUiConfig(DiscoveryClient discoveryClient, SwaggerUiConfigParameters swaggerUiConfigParameters) {
        return args -> {
            discoveryClient.getServices().stream()
                    .filter(serviceId -> !serviceId.equalsIgnoreCase("eureka-gateway"))
                    .toList()
                    .forEach(swaggerUiConfigParameters::addGroup);
        };
    }
}
