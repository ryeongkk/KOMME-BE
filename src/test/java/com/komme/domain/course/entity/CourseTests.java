package com.komme.domain.course.entity;

import java.time.LocalDate;
import java.util.Set;

import com.komme.domain.course.enums.Topic;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CourseTests {

    // 코스 엔티티 생성 시 필드가 그대로 저장되는지 검증
    @Test
    void createStoresAllFields() {
        User user = Mockito.mock(User.class);
        LocalDate visitDate = LocalDate.of(2026, 8, 10);

        Course course = Course.create(
                user, "성수동 먹방 Day", null, "성수동", "1", "2",
                Set.of(Topic.FOOD), visitDate
        );

        assertThat(course.getUser()).isSameAs(user);
        assertThat(course.getTitle()).isEqualTo("성수동 먹방 Day");
        assertThat(course.getDescription()).isNull();
        assertThat(course.getRegionName()).isEqualTo("성수동");
        assertThat(course.getAreaCode()).isEqualTo("1");
        assertThat(course.getSigunguCode()).isEqualTo("2");
        assertThat(course.getTopics()).containsExactly(Topic.FOOD);
        assertThat(course.getVisitDate()).isEqualTo(visitDate);
    }

    // LLM 생성 결과로 title/description이 갱신되는지 검증 (비동기 후속 갱신 경로)
    @Test
    void updateGeneratedContentOverwritesFallbackTitle() {
        Course course = Course.create(
                Mockito.mock(User.class),
                "성수동 먹방 Day", null, "성수동", "1", "2",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        );

        course.updateGeneratedContent("성수동에서 한국인처럼 먹방 여행하기", "골목골목 숨은 맛집을 따라가는 하루");

        assertThat(course.getTitle()).isEqualTo("성수동에서 한국인처럼 먹방 여행하기");
        assertThat(course.getDescription()).isEqualTo("골목골목 숨은 맛집을 따라가는 하루");
    }
}
