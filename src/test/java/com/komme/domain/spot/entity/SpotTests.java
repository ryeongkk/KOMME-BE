package com.komme.domain.spot.entity;

import java.math.BigDecimal;

import com.komme.domain.spot.enums.TimeSlot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpotTests {

    // 스팟 엔티티 생성 시 필드가 그대로 저장되는지 검증
    @Test
    void createStoresAllFields() {
        Spot spot = Spot.create("126508", attributes("성수동 카페", TimeSlot.LUNCH, "https://tong.visitkorea.or.kr/thumb.jpg"));

        assertThat(spot.getContentId()).isEqualTo("126508");
        assertThat(spot.getName()).isEqualTo("성수동 카페");
        assertThat(spot.getCategory1()).isEqualTo("A05");
        assertThat(spot.getTimeSlot()).isEqualTo(TimeSlot.LUNCH);
        assertThat(spot.getLatitude()).isEqualByComparingTo("37.5443300");
        assertThat(spot.getLongitude()).isEqualByComparingTo("127.0557800");
        assertThat(spot.getAreaCode()).isEqualTo("1");
        assertThat(spot.getSigunguCode()).isEqualTo("2");
        assertThat(spot.getThumbnailUrl()).isEqualTo("https://tong.visitkorea.or.kr/thumb.jpg");
    }

    // tourapi 재조회 결과로 필드가 갱신되는지 검증 (contentId는 갱신 대상 아님)
    @Test
    void refreshUpdatesMutableFieldsButKeepsContentId() {
        Spot spot = Spot.create("126508", attributes("성수동 카페", TimeSlot.LUNCH, null));

        spot.refresh(attributes("성수동 카페(개명)", TimeSlot.ANYTIME, "https://tong.visitkorea.or.kr/updated.jpg"));

        assertThat(spot.getContentId()).isEqualTo("126508");
        assertThat(spot.getName()).isEqualTo("성수동 카페(개명)");
        assertThat(spot.getTimeSlot()).isEqualTo(TimeSlot.ANYTIME);
        assertThat(spot.getThumbnailUrl()).isEqualTo("https://tong.visitkorea.or.kr/updated.jpg");
    }

    private Spot.Attributes attributes(String name, TimeSlot timeSlot, String thumbnailUrl) {
        return new Spot.Attributes(
                name,
                "A05", "A0502", "A05020900",
                timeSlot,
                new BigDecimal("37.5443300"), new BigDecimal("127.0557800"),
                "1", "2",
                thumbnailUrl
        );
    }
}
