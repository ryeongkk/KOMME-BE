package com.komme.domain.user.controller.docs;

import com.komme.common.response.ApiResponse;
import com.komme.domain.user.dto.request.ChangeNicknameRequest;
import com.komme.domain.user.dto.response.UserProfileResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "사용자 마이페이지 API")
public interface UserControllerDocs {

    // 마이페이지 프로필 조회 API
    @Operation(
            summary = "마이페이지 프로필 조회",
            description = "인증된 사용자의 닉네임, 연결 계정, 선호 언어, 마케팅 및 푸시 동의 상태를 조회합니다.",
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
}
