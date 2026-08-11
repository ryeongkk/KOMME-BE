package com.komme.domain.user.controller.docs;

import com.komme.common.response.ApiResponse;
import com.komme.domain.user.util.NicknamePolicy;
import com.komme.domain.user.dto.response.NicknameAvailabilityResponse;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.dto.request.ChangeNicknameRequest;
import com.komme.domain.user.dto.request.ChangePreferredLanguageRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "User", description = "사용자 마이페이지 API")
public interface UserControllerDocs {

    // 마이페이지 프로필 조회 API
    @Operation(
            summary = "마이페이지 프로필 조회",
            description = "인증된 사용자의 닉네임, 연결 계정, 선호 언어를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "프로필 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = UserProfileResponse.class),
                    examples = @ExampleObject(value = UserApiExamples.USER_PROFILE_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.INVALID_TOKEN))
    )
    @GetMapping("/me")
    ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    // 마이페이지 닉네임 변경 API
    @Operation(
            summary = "마이페이지 닉네임 변경",
            description = "인증된 사용자의 닉네임을 변경합니다. 본인의 기존 닉네임은 중복으로 보지 않습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "닉네임 변경 성공",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.SUCCESS_WITHOUT_DATA))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "닉네임 입력값 오류",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.INVALID_TOKEN))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 사용 중인 닉네임",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.NICKNAME_ALREADY_EXISTS))
    )
    @PatchMapping("/me/nickname")
    ResponseEntity<ApiResponse<Void>> changeNickname(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangeNicknameRequest request
    );

    // 닉네임 사용 가능 여부 조회 API
    @Operation(
            summary = "닉네임 사용 가능 여부 조회",
            description = "회원가입 또는 닉네임 변경 전에 닉네임 중복 여부를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "닉네임 사용 가능 여부 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = NicknameAvailabilityResponse.class),
                    examples = @ExampleObject(value = UserApiExamples.NICKNAME_AVAILABILITY_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "닉네임 입력값 오류",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.BAD_REQUEST))
    )
    @GetMapping("/nicknames/availability")
    ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(
            @RequestParam("nickname")
            @Pattern(regexp = NicknamePolicy.PATTERN, message = NicknamePolicy.MESSAGE)
            String nickname
    );

    // 마이페이지 선호 언어 변경 API
    @Operation(
            summary = "마이페이지 선호 언어 변경",
            description = "인증된 사용자의 선호 언어를 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "선호 언어 변경 성공",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.SUCCESS_WITHOUT_DATA))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "선호 언어 입력값 오류",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = @ExampleObject(value = UserApiExamples.INVALID_TOKEN))
    )
    @PatchMapping("/me/language")
    ResponseEntity<ApiResponse<Void>> changePreferredLanguage(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePreferredLanguageRequest request
    );
}
