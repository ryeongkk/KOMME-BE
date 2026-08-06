package com.komme.domain.course.mapping;

import java.util.Map;
import java.util.Optional;

import com.komme.domain.course.enums.Topic;

// tourapi cat1(대분류) 코드를 코스 주제(Topic)로 변환한다.
// spot의 CategoryTimeSlotMapper와 달리, 주제와 뚜렷이 연결되지 않는 카테고리(쇼핑/숙박/추천코스)는
// 폴백 없이 후보에서 제외한다 - 애매한 값을 억지로 아무 주제에나 우겨넣지 않기 위함.
public final class CategoryTopicMapper {

    private static final Map<String, Topic> CAT1_TOPICS = Map.of(
            "A01", Topic.HEALING,     // 자연
            "A02", Topic.EXPLORATION, // 인문(문화·예술·역사)
            "A03", Topic.EXPLORATION, // 레포츠
            "A05", Topic.FOOD         // 음식
            // A04(쇼핑), B02(숙박), C01(추천코스)는 의도적으로 매핑하지 않음 - resolve()가 empty를 반환
    );

    private CategoryTopicMapper() {
    }

    // cat1 코드로부터 Topic 결정 기능 - 매핑되지 않는 코드나 null은 empty(후보 제외 대상)
    public static Optional<Topic> resolve(String category1) {
        if (category1 == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CAT1_TOPICS.get(category1));
    }
}
