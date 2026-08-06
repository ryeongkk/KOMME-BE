package com.komme.domain.spot.mapping;

import java.math.BigDecimal;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.tourapi.client.LocationBasedListItem;

// tourapi 좌표기반 목록조회(locationBasedList2) 응답 item을 Spot.Attributes로 변환한다.
public final class SpotAttributesMapper {

    private SpotAttributesMapper() {
    }

    // locationBasedList2 응답 item 매핑 기능
    public static Spot.Attributes fromLocationBasedItem(LocationBasedListItem item) {
        return new Spot.Attributes(
                item.title(),
                item.category1(),
                item.category2(),
                item.category3(),
                CategoryTimeSlotMapper.resolve(item.category1(), item.title()),
                new BigDecimal(item.mapY()),
                new BigDecimal(item.mapX()),
                item.areaCode(),
                item.sigunguCode(),
                resolveThumbnailUrl(item)
        );
    }

    // 축소 썸네일(firstimage2)을 우선하고, 없으면 원본 이미지(firstimage)로 대체
    private static String resolveThumbnailUrl(LocationBasedListItem item) {
        if (item.firstImageThumbnail() != null && !item.firstImageThumbnail().isBlank()) {
            return item.firstImageThumbnail();
        }
        return item.firstImage();
    }
}
