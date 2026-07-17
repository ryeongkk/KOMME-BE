package com.komme.domain.auth.properties;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;

@Getter
@Validated
@ConfigurationProperties(prefix = "auth.google")
public class GoogleProperties {

    @NotBlank
    private final String clientId;

    @NotNull
    private final Duration jwksCacheTtl;

    // Google 로그인 설정값 생성
    public GoogleProperties(String clientId, Duration jwksCacheTtl) {
        this.clientId = clientId;
        this.jwksCacheTtl = jwksCacheTtl;
    }
}
