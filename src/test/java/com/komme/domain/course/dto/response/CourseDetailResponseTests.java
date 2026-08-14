package com.komme.domain.course.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.service.CourseGenerationResult;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CourseDetailResponseTests {

    // Course + CourseSpot 목록이 상세 응답으로 매핑되는지 검증
    @Test
    void ofMapsCourseAndCourseSpots() {
        Course course = Course.create(
                Mockito.mock(User.class), "성동구", "11", "11200",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        );

        CourseDetailResponse response = CourseDetailResponse.of(course, List.of());

        assertThat(response.regionName()).isEqualTo("성동구");
        assertThat(response.spots()).isEmpty();
    }

    // CourseGenerationResult로부터 응답이 생성되는지 검증 (생성 API 응답 경로)
    @Test
    void fromBuildsResponseFromGenerationResult() {
        Course course = Course.create(
                Mockito.mock(User.class), "성동구", "11", "11200",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        );
        CourseGenerationResult result = new CourseGenerationResult(course, List.of());

        CourseDetailResponse response = CourseDetailResponse.from(result);

        assertThat(response.regionName()).isEqualTo("성동구");
        assertThat(response.spots()).isEmpty();
    }
}
