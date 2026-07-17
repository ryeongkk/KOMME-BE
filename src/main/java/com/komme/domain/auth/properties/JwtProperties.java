package com.komme.domain.auth.properties;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;

@Getter
@Validated
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    @NotBlank
    private final String secret;

    @NotNull
    private final Duration accessTokenExpiration;

    @NotNull
    private final Duration refreshTokenExpiration;

    // JWT 설정값 생성
    public JwtProperties(
            String secret,
            Duration accessTokenExpiration,
            Duration refreshTokenExpiration
    ) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
