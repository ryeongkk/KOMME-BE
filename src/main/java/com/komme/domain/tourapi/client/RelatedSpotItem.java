package com.komme.domain.tourapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// 관광지별 연관 관광지 서비스(TarRlteTarService1) 응답 item
// ⚠ 정확한 응답 필드 목록을 매뉴얼로 확인하지 못해, KorService2 계열 응답에서 공통적으로 쓰이는 필드명을 기준으로
// 우선 작성했다. Stage 4에서 실제 응답 받아보고 필드명을 재확인/보정한다.
public record RelatedSpotItem(
        @JsonProperty("contentid") String contentId,
        @JsonProperty("title") String title,
        @JsonProperty("addr1") String address,
        @JsonProperty("mapx") String mapX,
        @JsonProperty("mapy") String mapY
) {
}
