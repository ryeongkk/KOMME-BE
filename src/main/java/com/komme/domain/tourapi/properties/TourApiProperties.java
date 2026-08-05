package com.komme.domain.tourapi.properties;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;

@Getter
@Validated
@ConfigurationProperties(prefix = "tour-api")
public class TourApiProperties {

    @NotBlank
    private final String serviceKey;

    @NotBlank
    private final String mobileOs;

    @NotBlank
    private final String mobileApp;

    // 한국관광공사 OpenAPI 공통 설정값 생성
    public TourApiProperties(String serviceKey, String mobileOs, String mobileApp) {
        this.serviceKey = serviceKey;
        this.mobileOs = mobileOs;
        this.mobileApp = mobileApp;
    }
}
