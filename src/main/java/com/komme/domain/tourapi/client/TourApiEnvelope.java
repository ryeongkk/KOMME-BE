package com.komme.domain.tourapi.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

// 공공데이터포털(관광공사) OpenAPI 공통 응답 봉투 - KorService2/연관관광지/집중률 서비스가 공통으로 쓰는 형태
public record TourApiEnvelope<T>(
        @JsonProperty("response") Response<T> response
) {

    public record Response<T>(
            @JsonProperty("header") Header header,
            @JsonProperty("body") Body<T> body
    ) {
    }

    public record Header(
            @JsonProperty("resultCode") String resultCode,
            @JsonProperty("resultMsg") String resultMsg
    ) {
        private static final String SUCCESS_CODE = "0000";

        // 정상 응답 여부 확인
        public boolean isSuccess() {
            return SUCCESS_CODE.equals(resultCode);
        }
    }

    public record Body<T>(
            @JsonProperty("items") Items<T> items,
            @JsonProperty("numOfRows") Integer numOfRows,
            @JsonProperty("pageNo") Integer pageNo,
            @JsonProperty("totalCount") Integer totalCount
    ) {
    }

    // items가 결과 없을 때 빈 문자열("")로 오는 경우가 있어 item이 null일 수 있음 - 호출부에서 null 방어 필요
    public record Items<T>(
            @JsonProperty("item") List<T> item
    ) {
    }
}
