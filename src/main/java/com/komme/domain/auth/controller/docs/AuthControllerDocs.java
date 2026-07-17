package com.komme.domain.auth.controller.docs;

import com.komme.common.response.ApiResponse;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.dto.response.TokenReissueResponse;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerDocs {

    // 이메일 인증 코드 전송 API
    @Operation(
            summary = "이메일 인증 코드 전송",
            description = "가입되지 않은 이메일로 6자리 인증 코드를 전송합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "인증 코드 전송 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(value = AuthApiExamples.SUCCESS_WITHOUT_DATA)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "이메일 입력값 오류",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 가입된 이메일",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.EMAIL_ALREADY_EXISTS))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "인증 이메일 전송 실패",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.EMAIL_SEND_FAILED))
    )
    @PostMapping("/email-verifications/send")
    ResponseEntity<ApiResponse<Void>> sendEmailVerification(
            @Valid @RequestBody EmailVerificationSendRequest request
    );

    // 이메일 인증 코드 확인 API
    @Operation(
            summary = "이메일 인증 코드 확인",
            description = "전송된 인증 코드를 확인하고 이메일 인증 상태를 저장합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "이메일 인증 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(value = AuthApiExamples.SUCCESS_WITHOUT_DATA)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "인증 코드 불일치 또는 만료",
            content = @Content(examples = {
                    @ExampleObject(
                            name = "인증 코드 불일치",
                            value = AuthApiExamples.INVALID_VERIFICATION_CODE
                    ),
                    @ExampleObject(
                            name = "인증 코드 만료",
                            value = AuthApiExamples.EXPIRED_VERIFICATION_CODE
                    )
            })
    )
    @PostMapping("/email-verifications/confirm")
    ResponseEntity<ApiResponse<Void>> confirmEmailVerification(
            @Valid @RequestBody EmailVerificationConfirmRequest request
    );

    // 이메일 회원가입 API
    @Operation(
            summary = "이메일 회원가입",
            description = "인증이 완료된 이메일과 비밀번호, 닉네임으로 LOCAL 계정을 생성합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(value = AuthApiExamples.SUCCESS_WITHOUT_DATA)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "회원가입 입력값 오류",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "이메일 인증 필요",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.EMAIL_NOT_VERIFIED))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이메일 또는 닉네임 중복",
            content = @Content(examples = {
                    @ExampleObject(
                            name = "이메일 중복",
                            value = AuthApiExamples.EMAIL_ALREADY_EXISTS
                    ),
                    @ExampleObject(
                            name = "닉네임 중복",
                            value = AuthApiExamples.NICKNAME_ALREADY_EXISTS
                    )
            })
    )
    @PostMapping("/signup")
    ResponseEntity<ApiResponse<Void>> signUp(
            @Valid @RequestBody SignUpRequest request
    );

    // 이메일 로그인 API
    @Operation(
            summary = "이메일 로그인",
            description = "LOCAL 계정의 이메일과 비밀번호를 검증하고 Access Token과 Refresh Token을 발급합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                    schema = @Schema(implementation = LoginResponse.class),
                    examples = @ExampleObject(value = AuthApiExamples.LOGIN_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "로그인 입력값 오류",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "이메일 또는 비밀번호 불일치",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.INVALID_CREDENTIALS))
    )
    @PostMapping("/login")
    ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    );

    // 토큰 재발급 API
    @Operation(
            summary = "토큰 재발급",
            description = "유효한 Refresh Token을 검증하고 새로운 Access Token과 Refresh Token을 발급합니다. 기존 Refresh Token은 즉시 폐기됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "토큰 재발급 성공",
            content = @Content(
                    schema = @Schema(implementation = TokenReissueResponse.class),
                    examples = @ExampleObject(value = AuthApiExamples.TOKEN_REISSUE_SUCCESS)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Refresh Token 누락",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "유효하지 않거나 만료된 Refresh Token",
            content = @Content(examples = {
                    @ExampleObject(
                            name = "유효하지 않은 토큰",
                            value = AuthApiExamples.INVALID_TOKEN
                    ),
                    @ExampleObject(
                            name = "만료된 토큰",
                            value = AuthApiExamples.EXPIRED_TOKEN
                    )
            })
    )
    @PostMapping("/tokens/reissue")
    ResponseEntity<ApiResponse<TokenReissueResponse>> reissueToken(
            @Valid @RequestBody TokenReissueRequest request
    );

    // 비밀번호 변경 API
    @Operation(
            summary = "비밀번호 변경",
            description = "현재 비밀번호를 확인하고 새로운 비밀번호로 변경합니다. 성공 시 모든 Refresh Token이 폐기됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "비밀번호 변경 성공",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.SUCCESS_WITHOUT_DATA))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "입력값 오류 또는 현재 비밀번호 불일치",
            content = @Content(examples = {
                    @ExampleObject(name = "입력값 오류", value = AuthApiExamples.BAD_REQUEST),
                    @ExampleObject(
                            name = "현재 비밀번호 불일치",
                            value = AuthApiExamples.INVALID_CURRENT_PASSWORD
                    )
            })
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 누락, 만료 또는 오류",
            content = @Content(examples = {
                    @ExampleObject(name = "유효하지 않은 토큰", value = AuthApiExamples.INVALID_TOKEN),
                    @ExampleObject(name = "만료된 토큰", value = AuthApiExamples.EXPIRED_TOKEN)
            })
    )
    @PatchMapping("/password")
    ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal TokenClaims tokenClaims,
            @Valid @RequestBody PasswordChangeRequest request
    );

    // 로그아웃 API
    @Operation(
            summary = "로그아웃",
            description = "현재 Access Token을 블랙리스트에 등록하고 요청한 기기의 Refresh Token을 폐기합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.SUCCESS_WITHOUT_DATA))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Refresh Token 누락",
            content = @Content(examples = @ExampleObject(value = AuthApiExamples.BAD_REQUEST))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Access Token 또는 Refresh Token 오류",
            content = @Content(examples = {
                    @ExampleObject(name = "유효하지 않은 토큰", value = AuthApiExamples.INVALID_TOKEN),
                    @ExampleObject(name = "만료된 토큰", value = AuthApiExamples.EXPIRED_TOKEN)
            })
    )
    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal TokenClaims tokenClaims,
            @Valid @RequestBody LogoutRequest request
    );
}
