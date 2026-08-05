package com.komme.domain.tourapi.exception;

import com.komme.common.base.status.BaseStatus;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TourApiErrorStatus implements BaseStatus {

    KOR_SERVICE_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "TOUR_502_1", "한국관광공사 국문 관광정보 서비스 응답이 올바르지 않습니다."),
    RELATED_SPOT_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "TOUR_502_2", "한국관광공사 연관 관광지 서비스 응답이 올바르지 않습니다."),
    CONCENTRATION_RATE_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "TOUR_502_3", "한국관광공사 관광지 집중률 서비스 응답이 올바르지 않습니다."),
    MULTILINGUAL_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "TOUR_502_4", "한국관광공사 다국어 관광정보 서비스 응답이 올바르지 않습니다."),
    KOR_SERVICE_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "TOUR_502_6", "한국관광공사 국문 관광정보 서비스 연결에 실패했습니다."),
    RELATED_SPOT_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "TOUR_502_7", "한국관광공사 연관 관광지 서비스 연결에 실패했습니다."),
    CONCENTRATION_RATE_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "TOUR_502_8", "한국관광공사 관광지 집중률 서비스 연결에 실패했습니다."),
    MULTILINGUAL_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "TOUR_502_9", "한국관광공사 다국어 관광정보 서비스 연결에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
