package com.komme.domain.course.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.common.response.ApiResponse;
import com.komme.domain.course.dto.request.CreateCourseRequest;
import com.komme.domain.course.dto.request.SaveCourseRequest;
import com.komme.domain.course.dto.response.CourseDetailResponse;
import com.komme.domain.course.dto.response.CourseSummaryResponse;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.CourseStatus;
import com.komme.domain.course.enums.SpotCount;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.service.CourseDeletionService;
import com.komme.domain.course.service.CourseGenerationResult;
import com.komme.domain.course.service.CourseGenerationService;
import com.komme.domain.course.service.CourseQueryService;
import com.komme.domain.course.service.CourseSaveService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseControllerTests {

    private CourseGenerationService courseGenerationService;
    private CourseQueryService courseQueryService;
    private CourseDeletionService courseDeletionService;
    private CourseSaveService courseSaveService;
    private CourseController courseController;

    // 코스 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        courseGenerationService = mock(CourseGenerationService.class);
        courseQueryService = mock(CourseQueryService.class);
        courseDeletionService = mock(CourseDeletionService.class);
        courseSaveService = mock(CourseSaveService.class);
        courseController = new CourseController(
                courseGenerationService, courseQueryService, courseDeletionService, courseSaveService
        );
    }

    // 코스 생성 API가 서비스 생성 결과를 응답 DTO로 변환해서 반환하는지 검증
    @Test
    void createCourseReturnsCourseDetailResponse() {
        CreateCourseRequest request = new CreateCourseRequest(
                "성수동", Set.of(Topic.FOOD), SpotCount.FOUR_OR_MORE, LocalDate.of(2026, 8, 10)
        );
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(1L);
        when(course.getTopics()).thenReturn(Set.of(Topic.FOOD));
        when(course.getVisitDate()).thenReturn(LocalDate.of(2026, 8, 10));
        when(course.getRegionName()).thenReturn("성동구");
        CourseGenerationResult result = new CourseGenerationResult(course, List.of());
        when(courseGenerationService.generate(1L, request)).thenReturn(result);

        ResponseEntity<ApiResponse<CourseDetailResponse>> response = courseController.createCourse(1L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getData().courseId()).isEqualTo(1L);
        assertThat(response.getBody().getData().regionName()).isEqualTo("성동구");
        verify(courseGenerationService).generate(1L, request);
    }

    // 코스 목록 조회 API가 CourseQueryService 결과를 그대로 반환하는지 검증
    @Test
    void getCoursesReturnsQueryServiceResult() {
        List<CourseSummaryResponse> summaries = List.of(
                new CourseSummaryResponse(1L, "성동구 음식 Day", "성동구", LocalDate.of(2026, 8, 10), Set.of(Topic.FOOD))
        );
        when(courseQueryService.findList(1L, CourseStatus.UPCOMING)).thenReturn(summaries);

        ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> response =
                courseController.getCourses(1L, CourseStatus.UPCOMING);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(summaries);
    }

    // 코스 상세 조회 API가 CourseQueryService 결과를 그대로 반환하는지 검증
    @Test
    void getCourseDetailReturnsQueryServiceResult() {
        CourseDetailResponse detail = new CourseDetailResponse(
                1L, "성동구", Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10), List.of()
        );
        when(courseQueryService.findDetail(1L, 10L)).thenReturn(detail);

        ResponseEntity<ApiResponse<CourseDetailResponse>> response = courseController.getCourseDetail(1L, 10L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(detail);
    }

    // 코스 삭제 API가 CourseDeletionService에 위임하는지 검증
    @Test
    void deleteCourseDelegatesToDeletionService() {
        ResponseEntity<ApiResponse<Void>> response = courseController.deleteCourse(1L, 10L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(courseDeletionService).delete(1L, 10L);
    }

    // 코스 저장 API가 CourseSaveService에 위임하는지 검증
    @Test
    void saveCourseDelegatesToSaveService() {
        SaveCourseRequest request = new SaveCourseRequest("성수동 데이트 코스");

        ResponseEntity<ApiResponse<Void>> response = courseController.saveCourse(1L, 10L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(courseSaveService).save(1L, 10L, request);
    }
}
