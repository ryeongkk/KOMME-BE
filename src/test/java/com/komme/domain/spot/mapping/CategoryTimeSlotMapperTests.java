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
}
