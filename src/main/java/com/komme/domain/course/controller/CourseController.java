package com.komme.domain.course.controller;

import com.komme.common.base.status.SuccessStatus;
import com.komme.common.response.ApiResponse;
import com.komme.domain.course.controller.docs.CourseControllerDocs;
import com.komme.domain.course.dto.request.CreateCourseRequest;
import com.komme.domain.course.dto.response.CourseDetailResponse;
import com.komme.domain.course.service.CourseGenerationResult;
import com.komme.domain.course.service.CourseGenerationService;

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
                request.duration(),
                request.visitDate()
        );
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, CourseDetailResponse.from(result));
    }
}
