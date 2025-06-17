package com.main.eureka.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    private final DiscoveryClient discoveryClient;


    /**
     * Eureka에 등록된 API를 동적으로 Swagger UI에 추가해주는 Bean
     * @param discoveryClient Eureka 서비스 탐색을 위한 클라이언트
     * @param swaggerUiConfigParameters Swagger UI 설정 파라미터
     * @return CommandLineRunner
     */
//    @Bean
//    public CommandLineRunner swaggerUiConfig(DiscoveryClient discoveryClient, SwaggerUiConfigParameters swaggerUiConfigParameters) {
//        return args -> {
//            discoveryClient.getServices().stream()
//                    .filter(serviceId -> !serviceId.equalsIgnoreCase("eureka-gateway"))
//                    .toList()
//                    .forEach(swaggerUiConfigParameters::addGroup);
//        };
//    }

//    @Bean
//    public OpenApiCustomizer serverUrlCustomizer() {
//        return openApi -> {
//            openApi.setServers(null);
//
//            openApi.addServersItem(new Server().url("/")
//                    .description("API Gateway"));
//
//            openApi.getPaths().forEach(((path, pathItem) -> {
//                pathItem.readOperations().forEach(operation -> {
//                    operation.setServers(null);
//                    openApi.addServersItem(new Server().url("/"));
//                });
//            }));
//        };
//    }

    @Bean
    public List<GroupedOpenApi> dynamicGroups() {
        return discoveryClient.getServices().stream()
                .filter(service -> !service.equals("eureka-gateway"))
                .map(service -> GroupedOpenApi.builder()
                        .group(service)
                        .pathsToMatch("/" + service + "/**")
                        .build())
                .collect(Collectors.toList());
    }

    @Bean
    public CommandLineRunner initGroups(SwaggerUiConfigParameters config) {
        return args -> {
            discoveryClient.getServices().stream()
                    .filter(service -> !service.equals("eureka-gateway")) // gateway 는 Swagger UI 그룹에서 제외
                    .forEach(config::addGroup);
        };
    }
}
