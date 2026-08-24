package com.komme.domain.course.controller.docs;

import java.util.List;

import com.komme.common.response.ApiResponse;
import com.komme.domain.course.dto.request.CreateCourseRequest;
import com.komme.domain.course.dto.request.SaveCourseRequest;
import com.komme.domain.course.dto.response.CourseDetailResponse;
import com.komme.domain.course.dto.response.CourseSummaryResponse;
import com.komme.domain.course.enums.CourseStatus;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Course", description = "코스 생성/조회/삭제 API")
public interface CourseControllerDocs {

    // 코스 생성 API
    @Operation(
            summary = "코스 생성",
            description = "지역 키워드로 중심 좌표를 찾고, 그 주변에서 주제에 맞는 스팟을 모아 시간대별 하루 코스를 생성합니다. "
                    + "서울/부산 지역만 지원하며, 스팟이 부족하면 반경을 자동으로 넓혀 재시도하고 그래도 부족하면 실패합니다.",
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
            description = "요청값 검증 실패 또는 서울/부산 외 지역",
            content = @Content(examples = {
                    @ExampleObject(name = "입력값 오류", value = CourseApiExamples.BAD_REQUEST),
                    @ExampleObject(name = "미지원 지역", value = CourseApiExamples.REGION_NOT_SUPPORTED)
            })
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.INVALID_TOKEN))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "지역을 찾을 수 없거나, 코스를 구성할 스팟이 부족함 (반경을 최대로 넓혀도 부족)",
            content = @Content(examples = {
                    @ExampleObject(name = "지역 없음", value = CourseApiExamples.REGION_NOT_FOUND),
                    @ExampleObject(name = "스팟 부족", value = CourseApiExamples.INSUFFICIENT_SPOTS)
            })
    )
    @PostMapping
    ResponseEntity<ApiResponse<CourseDetailResponse>> createCourse(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateCourseRequest request
    );

    // 코스 목록 조회 API
    @Operation(
            summary = "코스 목록 조회",
            description = "UPCOMING(D-day 임박순)/HISTORY(최근 완료순)로 인증된 사용자가 저장한 코스 목록을 조회합니다. "
                    + "저장(코스 저장 API 호출)하지 않은 코스는 목록에 뜨지 않습니다. "
                    + "별도 status 컬럼 없이 visitDate와 오늘 날짜를 비교해 조회 시점에 계산됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "코스 목록 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = CourseSummaryResponse.class),
                    examples = @ExampleObject(value = CourseApiExamples.LIST_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.INVALID_TOKEN))
    )
    @GetMapping
    ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getCourses(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "UPCOMING 또는 HISTORY") @RequestParam CourseStatus status
    );

    // 코스 상세 조회 API
    @Operation(
            summary = "코스 상세 조회",
            description = "코스의 스팟 타임라인(순서/시간대/다음 스팟까지 거리)을 포함한 상세 정보를 조회합니다. "
                    + "본인 코스가 아니면 존재 여부를 숨기기 위해 404로 응답합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "코스 상세 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = CourseDetailResponse.class),
                    examples = @ExampleObject(value = CourseApiExamples.DETAIL_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.INVALID_TOKEN))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "코스가 없거나 본인 코스가 아님",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.COURSE_NOT_FOUND))
    )
    @GetMapping("/{courseId}")
    ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId
    );

    // 코스 삭제 API
    @Operation(
            summary = "코스 삭제",
            description = "코스를 하드 삭제합니다. 본인 코스가 아니면 존재 여부를 숨기기 위해 404로 응답합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "코스 삭제 성공",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.SUCCESS_WITHOUT_DATA))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.INVALID_TOKEN))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "코스가 없거나 본인 코스가 아님",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.COURSE_NOT_FOUND))
    )
    @DeleteMapping("/{courseId}")
    ResponseEntity<ApiResponse<Void>> deleteCourse(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId
    );

    // 코스 저장 API
    @Operation(
            summary = "코스 저장",
            description = "생성된 코스에 사용자가 입력한 이름을 붙여 저장합니다. 저장해야 코스 목록(다가오는 코스/지난 코스)에 노출됩니다. "
                    + "이미 저장한 코스를 다시 저장하면 이름만 갱신됩니다. 생성자 본인이 아니면 존재 여부를 숨기기 위해 404로 응답합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "코스 저장 성공",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.SUCCESS_WITHOUT_DATA))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "코스명 입력값 오류",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.INVALID_TOKEN))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "코스가 없거나 본인 코스가 아님",
            content = @Content(examples = @ExampleObject(value = CourseApiExamples.COURSE_NOT_FOUND))
    )
    @PatchMapping("/{courseId}/save")
    ResponseEntity<ApiResponse<Void>> saveCourse(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId,
            @Valid @RequestBody SaveCourseRequest request
    );
}
