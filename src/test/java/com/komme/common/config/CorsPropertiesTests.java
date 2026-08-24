package com.komme.common.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorsPropertiesTests {

    // 쉼표 기반 허용 Origin 목록 생성 검증
    @Test
    void getAllowedOriginListParsesCommaSeparatedOrigins() {
        CorsProperties corsProperties = new CorsProperties(
                "http://localhost:3000, https://komme.example.com, "
        );

        assertThat(corsProperties.getAllowedOriginList())
                .containsExactly("http://localhost:3000", "https://komme.example.com");
    }
}
