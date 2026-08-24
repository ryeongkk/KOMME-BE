package com.komme.domain.course.dto.response;

import java.time.LocalDate;
import java.util.Set;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.UserCourse;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CourseSummaryResponseTests {

    // UserCourse(저장된 코스) 필드가 목록용 응답으로 그대로 매핑되는지 검증
    @Test
    void ofMapsUserCourseFields() {
        Course course = Course.create(
                Mockito.mock(User.class), "성동구", "11", "11200",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        );
        UserCourse userCourse = UserCourse.create(Mockito.mock(User.class), course, "성동구 음식 Day");

        CourseSummaryResponse response = CourseSummaryResponse.of(userCourse, 0L);

        assertThat(response.title()).isEqualTo("성동구 음식 Day");
        assertThat(response.regionName()).isEqualTo("성동구");
        assertThat(response.visitDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.topics()).containsExactly(Topic.FOOD);
        assertThat(response.spotCount()).isZero();
    }

    // 스팟 개수가 목록용 응답으로 매핑되는지 검증
    @Test
    void ofMapsSpotCount() {
        Course course = Course.create(
                Mockito.mock(User.class), "성동구", "11", "11200",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        );
        UserCourse userCourse = UserCourse.create(Mockito.mock(User.class), course, "성동구 음식 Day");

        CourseSummaryResponse response = CourseSummaryResponse.of(userCourse, 4L);

        assertThat(response.spotCount()).isEqualTo(4L);
    }
}
