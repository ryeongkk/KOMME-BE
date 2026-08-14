package com.komme.domain.course.controller;

import java.util.List;

import com.komme.common.base.status.SuccessStatus;
import com.komme.common.response.ApiResponse;
import com.komme.domain.course.controller.docs.CourseControllerDocs;
import com.komme.domain.course.dto.request.CreateCourseRequest;
import com.komme.domain.course.dto.response.CourseDetailResponse;
import com.komme.domain.course.dto.response.CourseSummaryResponse;
import com.komme.domain.course.enums.CourseStatus;
import com.komme.domain.course.service.CourseDeletionService;
import com.komme.domain.course.service.CourseGenerationResult;
import com.komme.domain.course.service.CourseGenerationService;
import com.komme.domain.course.service.CourseQueryService;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController implements CourseControllerDocs {

    private final CourseGenerationService courseGenerationService;
    private final CourseQueryService courseQueryService;
    private final CourseDeletionService courseDeletionService;

    // 코스 생성 API
    @Override
    public ResponseEntity<ApiResponse<CourseDetailResponse>> createCourse(
            Long userId,
            CreateCourseRequest request
    ) {
        CourseGenerationResult result = courseGenerationService.generate(
                userId,
                request.longitude(),
                request.latitude(),
                request.topics(),
                request.spotCount().getValue(),
                request.visitDate()
        );
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, CourseDetailResponse.from(result));
    }

    // 코스 목록 조회 API
    @Override
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getCourses(
            Long userId,
            CourseStatus status
    ) {
        List<CourseSummaryResponse> response = courseQueryService.findList(userId, status);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, response);
    }

    // 코스 상세 조회 API
    @Override
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            Long userId,
            Long courseId
    ) {
        CourseDetailResponse response = courseQueryService.findDetail(userId, courseId);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, response);
    }

    // 코스 삭제 API
    @Override
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            Long userId,
            Long courseId
    ) {
        courseDeletionService.delete(userId, courseId);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }
}
