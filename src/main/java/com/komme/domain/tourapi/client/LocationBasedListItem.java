package com.komme.domain.tourapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// KorService2 locationBasedList2(좌표기반 목록조회) 응답 item - areaBasedList2와 거의 동일하되 기준 좌표로부터의 거리(dist)가 추가로 내려옴
public record LocationBasedListItem(
        @JsonProperty("contentid") String contentId,
        @JsonProperty("contenttypeid") String contentTypeId,
        @JsonProperty("title") String title,
        @JsonProperty("addr1") String address,
        @JsonProperty("areacode") String areaCode,
        @JsonProperty("sigungucode") String sigunguCode,
        @JsonProperty("cat1") String category1,
        @JsonProperty("cat2") String category2,
        @JsonProperty("cat3") String category3,
        @JsonProperty("mapx") String mapX,
        @JsonProperty("mapy") String mapY,
        @JsonProperty("firstimage") String firstImage,
        @JsonProperty("firstimage2") String firstImageThumbnail,
        @JsonProperty("dist") String distanceMeters
) {
}
