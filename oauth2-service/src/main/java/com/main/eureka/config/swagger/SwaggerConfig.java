package com.main.eureka.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(@Value("${openapi.service.url}") String url) {
        return new OpenAPI()
                .info(new Info()
                        .title("OAuth2 API Service")
                        .version("1.0"))
                .servers(List.of(new Server().url(url)));
    }
}
