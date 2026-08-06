package com.komme.domain.spot.mapping;

import java.util.Map;

import com.komme.domain.spot.enums.TimeSlot;

// tourapi cat1(대분류) 코드를 TimeSlot으로 변환한다.
// cat1은 7개뿐인 대분류라 대부분 아침/저녁을 구분할 근거가 없어, 음식(A05)만 LUNCH로 구분하고 나머지는 ANYTIME으로 둔다.
// 더 세밀한 시간대 구분(아침식사/디너, 오전 박물관/야시장 등)은 cat2/cat3 매핑이 필요해 이번 스코프에서는 다루지 않는다.
public final class CategoryTimeSlotMapper {

    private static final Map<String, TimeSlot> CAT1_TIME_SLOTS = Map.of(
            "A01", TimeSlot.ANYTIME, // 자연
            "A02", TimeSlot.ANYTIME, // 인문(문화·예술·역사)
            "A03", TimeSlot.ANYTIME, // 레포츠
            "A04", TimeSlot.ANYTIME, // 쇼핑
            "A05", TimeSlot.LUNCH,   // 음식
            "B02", TimeSlot.ANYTIME, // 숙박
            "C01", TimeSlot.ANYTIME  // 추천코스
    );

    private CategoryTimeSlotMapper() {
    }

    // cat1 코드로부터 TimeSlot 결정 기능 - 알 수 없는 코드나 null은 ANYTIME으로 폴백
    public static TimeSlot resolve(String category1) {
        if (category1 == null) {
            return TimeSlot.ANYTIME;
        }
        return CAT1_TIME_SLOTS.getOrDefault(category1, TimeSlot.ANYTIME);
    }
}
