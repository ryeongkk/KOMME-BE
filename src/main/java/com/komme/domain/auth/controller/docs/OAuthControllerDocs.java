package com.komme.domain.auth.controller.docs;

import com.komme.common.response.ApiResponse;
import com.komme.domain.auth.dto.request.OAuthAppleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthGoogleLoginRequest;
import com.komme.domain.auth.dto.response.LoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "OAuth", description = "소셜 로그인 API")
public interface OAuthControllerDocs {

    // Apple 로그인 API
    @Operation(
            summary = "Apple 로그인",
            description = "Apple identity token의 서명, 발급자, 대상, 만료를 검증합니다. "
                    + "연결된 계정은 로그인하고, 검증된 이메일의 기존 계정은 Apple 계정을 연결하며, "
                    + "가입 이력이 없으면 최소 프로필의 Apple 계정을 생성합니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = OAuthAppleLoginRequest.class),
                    examples = @ExampleObject(value = OAuthApiExamples.APPLE_LOGIN_REQUEST)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Apple 로그인 또는 회원가입 성공",
            content = @Content(
                    schema = @Schema(implementation = LoginResponse.class),
                    examples = @ExampleObject(value = OAuthApiExamples.APPLE_LOGIN_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "identity token 누락 또는 신규 가입에 필요한 이메일 없음",
            content = @Content(examples = {
                    @ExampleObject(name = "입력값 오류", value = AuthApiExamples.BAD_REQUEST),
                    @ExampleObject(
                            name = "Apple 이메일 없음",
                            value = OAuthApiExamples.APPLE_EMAIL_REQUIRED
                    )
            })
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "서명, 발급자, 대상 또는 만료 검증 실패",
            content = @Content(
                    examples = @ExampleObject(
                            value = OAuthApiExamples.INVALID_APPLE_IDENTITY_TOKEN
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "동시에 다른 사용자에게 연결된 Apple 계정",
            content = @Content(
                    examples = @ExampleObject(
                            value = OAuthApiExamples.OAUTH_ACCOUNT_ALREADY_LINKED
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "502",
            description = "Apple 공개키 서버 연결 실패",
            content = @Content(
                    examples = @ExampleObject(
                            value = OAuthApiExamples.APPLE_SERVER_CONNECTION_FAILED
                    )
            )
    )
    @PostMapping("/apple")
    ResponseEntity<ApiResponse<LoginResponse>> loginWithApple(
            @Valid @RequestBody OAuthAppleLoginRequest request
    );

    // Google 로그인 API
    @Operation(
            summary = "Google 로그인",
            description = "Google ID token의 서명, 발급자, 대상, 만료를 검증합니다. "
                    + "연결된 계정은 로그인하고, 검증된 이메일의 기존 계정은 Google 계정을 연결하며, "
                    + "가입 이력이 없으면 최소 프로필의 Google 계정을 생성합니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = OAuthGoogleLoginRequest.class),
                    examples = @ExampleObject(value = OAuthApiExamples.GOOGLE_LOGIN_REQUEST)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Google 로그인 또는 회원가입 성공",
            content = @Content(
                    schema = @Schema(implementation = LoginResponse.class),
                    examples = @ExampleObject(value = OAuthApiExamples.GOOGLE_LOGIN_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "ID token 누락 또는 이메일 정보 없음",
            content = @Content(examples = {
                    @ExampleObject(name = "입력값 오류", value = AuthApiExamples.BAD_REQUEST),
                    @ExampleObject(
                            name = "Google 이메일 없음",
                            value = OAuthApiExamples.GOOGLE_EMAIL_REQUIRED
                    )
            })
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Google ID token 검증 실패",
            content = @Content(
                    examples = @ExampleObject(
                            value = OAuthApiExamples.INVALID_GOOGLE_IDENTITY_TOKEN
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "동시에 다른 사용자에게 연결된 Google 계정",
            content = @Content(
                    examples = @ExampleObject(
                            value = OAuthApiExamples.OAUTH_ACCOUNT_ALREADY_LINKED
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "502",
            description = "Google 공개키 서버 연결 실패",
            content = @Content(
                    examples = @ExampleObject(
                            value = OAuthApiExamples.GOOGLE_SERVER_CONNECTION_FAILED
                    )
            )
    )
    @PostMapping("/google")
    ResponseEntity<ApiResponse<LoginResponse>> loginWithGoogle(
            @Valid @RequestBody OAuthGoogleLoginRequest request
    );
}
