package com.komme.domain.spot.mapping;

import java.math.BigDecimal;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;
import com.komme.domain.tourapi.client.LocationBasedListItem;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpotAttributesMapperTests {

    // 정상 응답 item이 Attributes로 그대로 매핑되는지 검증 (이름에 "카페" 포함 -> MORNING, 이름 키워드가 cat1 매핑보다 우선)
    @Test
    void fromLocationBasedItemMapsAllFields() {
        LocationBasedListItem item = new LocationBasedListItem(
                "126508", "39", "성수동 카페", "서울 성동구 성수동2가",
                "1", "2",
                "A05", "A0502", "A05020900",
                "127.0557800", "37.5443300",
                "https://tong.visitkorea.or.kr/full.jpg", "https://tong.visitkorea.or.kr/thumb.jpg",
                "820"
        );

        Spot.Attributes attributes = SpotAttributesMapper.fromLocationBasedItem(item);

        assertThat(attributes.name()).isEqualTo("성수동 카페");
        assertThat(attributes.category1()).isEqualTo("A05");
        assertThat(attributes.timeSlot()).isEqualTo(TimeSlot.MORNING);
        assertThat(attributes.latitude()).isEqualByComparingTo("37.5443300");
        assertThat(attributes.longitude()).isEqualByComparingTo("127.0557800");
        assertThat(attributes.areaCode()).isEqualTo("1");
        assertThat(attributes.sigunguCode()).isEqualTo("2");
        assertThat(attributes.thumbnailUrl()).isEqualTo("https://tong.visitkorea.or.kr/thumb.jpg");
    }

    // 축소 썸네일이 비어있으면 원본 이미지로 대체되는지 검증
    @Test
    void fromLocationBasedItemFallsBackToFullImageWhenThumbnailBlank() {
        LocationBasedListItem item = new LocationBasedListItem(
                "126508", "39", "성수동 카페", "서울 성동구 성수동2가",
                "1", "2",
                "A05", "A0502", "A05020900",
                "127.0557800", "37.5443300",
                "https://tong.visitkorea.or.kr/full.jpg", "",
                "820"
        );

        Spot.Attributes attributes = SpotAttributesMapper.fromLocationBasedItem(item);

        assertThat(attributes.thumbnailUrl()).isEqualTo("https://tong.visitkorea.or.kr/full.jpg");
    }
}
