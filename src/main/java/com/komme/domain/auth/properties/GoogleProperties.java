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

    @NotBlank
    private final String clientSecret;

    // authorization code 교환 시 redirect_uri로 보낼 값.
    // GIS(Google Identity Services) initCodeClient(ux_mode: 'popup')는 리다이렉트가 없고,
    // 실제 콜백 URL이 아니라 로그인 버튼을 호출한 페이지의 origin(스킴+호스트[+포트], path 없음)을
    // 그대로 요구한다 - Google Console의 "승인된 자바스크립트 원본"과 동일한 값이어야 한다.
    @NotBlank
    private final String redirectUri;

    @NotNull
    private final Duration jwksCacheTtl;

    // Google 로그인 설정값 생성
    public GoogleProperties(
            String clientId,
            String clientSecret,
            String redirectUri,
            Duration jwksCacheTtl
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.jwksCacheTtl = jwksCacheTtl;
    }
}
