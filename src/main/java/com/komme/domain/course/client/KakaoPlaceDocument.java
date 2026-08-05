package com.komme.domain.course.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// 카카오 로컬 API 장소 검색 결과 항목 - 좌표는 WGS84(경도 x / 위도 y)
public record KakaoPlaceDocument(
        @JsonProperty("place_name") String placeName,
        @JsonProperty("address_name") String addressName,
        @JsonProperty("road_address_name") String roadAddressName,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("category_group_code") String categoryGroupCode,
        @JsonProperty("x") String longitude,
        @JsonProperty("y") String latitude
) {
}
