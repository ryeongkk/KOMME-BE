package com.komme.domain.course.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.common.response.ApiResponse;
import com.komme.domain.course.dto.request.CreateCourseRequest;
import com.komme.domain.course.dto.response.CourseDetailResponse;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.Duration;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.service.CourseGenerationResult;
import com.komme.domain.course.service.CourseGenerationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseControllerTests {

    private CourseGenerationService courseGenerationService;
    private CourseController courseController;

    // 코스 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        courseGenerationService = mock(CourseGenerationService.class);
        courseController = new CourseController(courseGenerationService);
    }

    // 코스 생성 API가 서비스 생성 결과를 응답 DTO로 변환해서 반환하는지 검증
    @Test
    void createCourseReturnsCourseDetailResponse() {
        CreateCourseRequest request = new CreateCourseRequest(
                new BigDecimal("127.05578"), new BigDecimal("37.54433"),
                Set.of(Topic.FOOD), Duration.HALF_DAY, LocalDate.of(2026, 8, 10)
        );
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(1L);
        when(course.getTitle()).thenReturn("성동구 먹방 Day");
        when(course.getTopics()).thenReturn(Set.of(Topic.FOOD));
        when(course.getVisitDate()).thenReturn(LocalDate.of(2026, 8, 10));
        when(course.getRegionName()).thenReturn("성동구");
        CourseGenerationResult result = new CourseGenerationResult(course, List.of());
        when(courseGenerationService.generate(
                1L, request.longitude(), request.latitude(), request.topics(), request.duration(), request.visitDate()
        )).thenReturn(result);

        ResponseEntity<ApiResponse<CourseDetailResponse>> response = courseController.createCourse(1L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getData().courseId()).isEqualTo(1L);
        assertThat(response.getBody().getData().title()).isEqualTo("성동구 먹방 Day");
        assertThat(response.getBody().getData().regionName()).isEqualTo("성동구");
        verify(courseGenerationService).generate(
                1L, request.longitude(), request.latitude(), request.topics(), request.duration(), request.visitDate()
        );
    }
}
