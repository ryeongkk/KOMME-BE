package com.komme.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTests {

    // Swagger OpenAPI 기본 정보 검증
    @Test
    void swaggerConfigDefinesOpenApiInfo() {
        OpenAPIDefinition definition =
                SwaggerConfig.class.getAnnotation(OpenAPIDefinition.class);

        assertThat(definition.info().title()).isEqualTo("KOMME API");
        assertThat(definition.info().version()).isEqualTo("v1");
    }

    // Swagger Bearer 인증 스키마 검증
    @Test
    void swaggerConfigDefinesBearerAuthScheme() {
        SecurityScheme securityScheme =
                SwaggerConfig.class.getAnnotation(SecurityScheme.class);

        assertThat(securityScheme.name()).isEqualTo("bearerAuth");
        assertThat(securityScheme.type()).isEqualTo(SecuritySchemeType.HTTP);
        assertThat(securityScheme.scheme()).isEqualTo("bearer");
        assertThat(securityScheme.bearerFormat()).isEqualTo("JWT");
    }
}
