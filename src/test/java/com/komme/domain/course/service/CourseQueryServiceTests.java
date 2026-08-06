package com.komme.domain.course.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.domain.course.dto.response.CourseSummaryResponse;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.CourseStatus;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseQueryServiceTests {

    private static final Long USER_ID = 1L;

    @Mock
    private CourseRepository courseRepository;

    // UPCOMING 조회 시 오름차순(D-day 임박순) 조회 메서드를 타는지 검증
    @Test
    void findListUsesAscendingQueryForUpcoming() {
        CourseQueryService service = new CourseQueryService(courseRepository);
        Course course = course("성동구 먹방 Day", LocalDate.of(2026, 8, 10));
        when(courseRepository.findByUser_IdAndVisitDateGreaterThanEqualOrderByVisitDateAsc(eq(USER_ID), any()))
                .thenReturn(List.of(course));

        List<CourseSummaryResponse> result = service.findList(USER_ID, CourseStatus.UPCOMING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("성동구 먹방 Day");
    }

    // HISTORY 조회 시 내림차순(최근 완료순) 조회 메서드를 타는지 검증
    @Test
    void findListUsesDescendingQueryForHistory() {
        CourseQueryService service = new CourseQueryService(courseRepository);
        Course course = course("성동구 힐링 Day", LocalDate.of(2026, 7, 1));
        when(courseRepository.findByUser_IdAndVisitDateLessThanOrderByVisitDateDesc(eq(USER_ID), any()))
                .thenReturn(List.of(course));

        List<CourseSummaryResponse> result = service.findList(USER_ID, CourseStatus.HISTORY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("성동구 힐링 Day");
    }

    private Course course(String title, LocalDate visitDate) {
        return Course.create(
                Mockito.mock(User.class), title, null, "성동구", "11", "11200",
                Set.of(Topic.FOOD), visitDate
        );
    }
}
