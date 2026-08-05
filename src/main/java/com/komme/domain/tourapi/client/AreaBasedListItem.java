package com.komme.domain.tourapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// KorService2 areaBasedList2(지역기반 목록조회) 응답 item
public record AreaBasedListItem(
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
        @JsonProperty("firstimage2") String firstImageThumbnail
) {
}
