package com.komme.domain.course.controller.docs;

import com.komme.common.response.ApiResponse;
import com.komme.domain.course.dto.request.CreateCourseRequest;
import com.komme.domain.course.dto.response.CourseDetailResponse;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Course", description = "코스 생성/조회/삭제 API")
public interface CourseControllerDocs {

    // 코스 생성 API
    @Operation(
            summary = "코스 생성",
            description = "좌표+반경 기준으로 주제에 맞는 스팟을 모아 시간대별 하루 코스를 생성합니다. "
                    + "스팟이 부족하면 반경을 자동으로 넓혀 재시도하고, 그래도 부족하면 실패합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "코스 생성 성공",
            content = @Content(
                    schema = @Schema(implementation = CourseDetailResponse.class),
                    examples = @ExampleObject(value = CourseApiExamples.CREATE_COURSE_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청값 검증 실패",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.INVALID_TOKEN))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "코스를 구성할 스팟이 부족함 (반경을 최대로 넓혀도 부족)",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.INSUFFICIENT_SPOTS))
    )
    @PostMapping
    ResponseEntity<ApiResponse<CourseDetailResponse>> createCourse(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateCourseRequest request
    );
}
