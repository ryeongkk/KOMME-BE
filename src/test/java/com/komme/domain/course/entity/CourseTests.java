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
                user, "성수동 음식 Day", "성수동", "1", "2",
                Set.of(Topic.FOOD), visitDate
        );

        assertThat(course.getUser()).isSameAs(user);
        assertThat(course.getTitle()).isEqualTo("성수동 음식 Day");
        assertThat(course.getRegionName()).isEqualTo("성수동");
        assertThat(course.getAreaCode()).isEqualTo("1");
        assertThat(course.getSigunguCode()).isEqualTo("2");
        assertThat(course.getTopics()).containsExactly(Topic.FOOD);
        assertThat(course.getVisitDate()).isEqualTo(visitDate);
    }
}
