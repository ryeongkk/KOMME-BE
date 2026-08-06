package com.komme.domain.spot.mapping;

import java.util.Map;
import java.util.Set;

import com.komme.domain.spot.enums.TimeSlot;

// tourapi cat1(대분류) 코드를 TimeSlot으로 변환한다.
// cat1만으로는 MORNING/EVENING을 만들어낼 근거가 없어(7개 대분류 중 음식만 LUNCH로 구분 가능, 나머지는 ANYTIME),
// 실제 cat2/cat3 코드는 검증되지 않은 상태라 스팟 이름의 키워드로 아침/저녁을 우선 판별하고, 그 외엔 cat1 매핑으로 폴백한다.
// 실제 서비스키로 cat2/cat3 코드를 확인하면 이 키워드 방식을 정식 코드 매핑으로 교체해야 한다.
public final class CategoryTimeSlotMapper {

    private static final Set<String> MORNING_NAME_KEYWORDS = Set.of("카페", "베이커리", "브런치");
    private static final Set<String> EVENING_NAME_KEYWORDS = Set.of("포차", "호프", "술집", "치킨", "바");

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

    // 스팟 이름 키워드 우선 판별 후 cat1로 폴백하는 TimeSlot 결정 기능
    public static TimeSlot resolve(String category1, String name) {
        if (containsAny(name, MORNING_NAME_KEYWORDS)) {
            return TimeSlot.MORNING;
        }
        if (containsAny(name, EVENING_NAME_KEYWORDS)) {
            return TimeSlot.EVENING;
        }
        return resolve(category1);
    }

    // cat1 코드로부터 TimeSlot 결정 기능 - 알 수 없는 코드나 null은 ANYTIME으로 폴백
    public static TimeSlot resolve(String category1) {
        if (category1 == null) {
            return TimeSlot.ANYTIME;
        }
        return CAT1_TIME_SLOTS.getOrDefault(category1, TimeSlot.ANYTIME);
    }

    private static boolean containsAny(String name, Set<String> keywords) {
        if (name == null) {
            return false;
        }
        return keywords.stream().anyMatch(name::contains);
    }
}
