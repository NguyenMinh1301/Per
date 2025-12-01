package com.per.common.config;

import java.util.List;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Per API")
                                .version("3.0")
                                .description("Per API Documentation")
                                .license(
                                        new License()
                                                .name("API license")
                                                .url(
                                                        "https://github.com/NguyenMinh1301/Per/blob/master/LICENSE")))
                .servers(List.of(new Server().url("https://nguyenminh.space")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")))
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }

    @Bean
    public GroupedOpenApi auth() {
        return GroupedOpenApi.builder()
                .group("Auth")
                .packagesToScan("com.per.auth")
                .pathsToMatch("/per/**")
                .build();
    }

    @Bean
    public GroupedOpenApi user() {
        return GroupedOpenApi.builder()
                .group("User")
                .packagesToScan("com.per.user")
                .pathsToMatch("/per/**")
                .build();
    }

    @Bean
    public GroupedOpenApi product() {
        return GroupedOpenApi.builder()
                .group("Product")
                .packagesToScan("com.per.product")
                .pathsToMatch("/per/**")
                .build();
    }

    @Bean
    public GroupedOpenApi brand() {
        return GroupedOpenApi.builder()
                .group("Brand")
                .packagesToScan("com.per.brand")
                .pathsToMatch("/per/**")
                .build();
    }

    @Bean
    public GroupedOpenApi category() {
        return GroupedOpenApi.builder()
                .group("Category")
                .packagesToScan("com.per.category")
                .pathsToMatch("/per/**")
                .build();
    }

    @Bean
    public GroupedOpenApi made_in() {
        return GroupedOpenApi.builder()
                .group("Made_in")
                .packagesToScan("com.per.made_in")
                .pathsToMatch("/per/**")
                .build();
    }

    @Bean
    public GroupedOpenApi media() {
        return GroupedOpenApi.builder()
                .group("Media")
                .packagesToScan("com.per.media")
                .pathsToMatch("/per/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cart() {
        return GroupedOpenApi.builder()
                .group("Cart")
                .packagesToScan("com.per.cart")
                .pathsToMatch("/per/**")
                .build();
    }

    @Bean
    public GroupedOpenApi payment() {
        return GroupedOpenApi.builder()
                .group("Payment")
                .packagesToScan("com.per.payment")
                .pathsToMatch("/**")
                .build();
    }
}
