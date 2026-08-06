package com.komme.domain.spot.mapping;

import com.komme.domain.spot.enums.TimeSlot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTimeSlotMapperTests {

    // 음식(A05)만 LUNCH로 구분되는지 검증
    @Test
    void resolveMapsFoodCategoryToLunch() {
        assertThat(CategoryTimeSlotMapper.resolve("A05")).isEqualTo(TimeSlot.LUNCH);
    }

    // 나머지 대분류는 전부 ANYTIME인지 검증
    @Test
    void resolveMapsOtherKnownCategoriesToAnytime() {
        assertThat(CategoryTimeSlotMapper.resolve("A01")).isEqualTo(TimeSlot.ANYTIME);
        assertThat(CategoryTimeSlotMapper.resolve("A02")).isEqualTo(TimeSlot.ANYTIME);
        assertThat(CategoryTimeSlotMapper.resolve("A03")).isEqualTo(TimeSlot.ANYTIME);
        assertThat(CategoryTimeSlotMapper.resolve("A04")).isEqualTo(TimeSlot.ANYTIME);
        assertThat(CategoryTimeSlotMapper.resolve("B02")).isEqualTo(TimeSlot.ANYTIME);
        assertThat(CategoryTimeSlotMapper.resolve("C01")).isEqualTo(TimeSlot.ANYTIME);
    }

    // 알 수 없는 코드나 null은 ANYTIME으로 폴백되는지 검증
    @Test
    void resolveFallsBackToAnytimeForUnknownOrNullCategory() {
        assertThat(CategoryTimeSlotMapper.resolve("Z99")).isEqualTo(TimeSlot.ANYTIME);
        assertThat(CategoryTimeSlotMapper.resolve(null)).isEqualTo(TimeSlot.ANYTIME);
    }

    // 이름에 아침 키워드가 있으면 cat1과 무관하게 MORNING으로 판별되는지 검증
    @Test
    void resolveWithNameDetectsMorningKeyword() {
        assertThat(CategoryTimeSlotMapper.resolve("A01", "성수동 브런치 카페")).isEqualTo(TimeSlot.MORNING);
        assertThat(CategoryTimeSlotMapper.resolve("A05", "동네 베이커리")).isEqualTo(TimeSlot.MORNING);
    }

    // 이름에 저녁 키워드가 있으면 cat1과 무관하게 EVENING으로 판별되는지 검증
    @Test
    void resolveWithNameDetectsEveningKeyword() {
        assertThat(CategoryTimeSlotMapper.resolve("A05", "을지로 포차")).isEqualTo(TimeSlot.EVENING);
        assertThat(CategoryTimeSlotMapper.resolve("A03", "동네 호프집")).isEqualTo(TimeSlot.EVENING);
    }

    // 이름에 키워드가 없으면 기존 cat1 매핑으로 폴백되는지 검증
    @Test
    void resolveWithNameFallsBackToCategoryWhenNoKeywordMatches() {
        assertThat(CategoryTimeSlotMapper.resolve("A05", "성수동 맛집")).isEqualTo(TimeSlot.LUNCH);
        assertThat(CategoryTimeSlotMapper.resolve("A01", "성수동 공원")).isEqualTo(TimeSlot.ANYTIME);
        assertThat(CategoryTimeSlotMapper.resolve("A05", null)).isEqualTo(TimeSlot.LUNCH);
    }
}
