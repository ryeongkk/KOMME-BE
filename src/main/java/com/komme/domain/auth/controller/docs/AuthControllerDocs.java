package com.komme.domain.auth.controller.docs;

import com.komme.common.response.ApiResponse;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerDocs {

    // 이메일 인증 코드 전송 API
    @Operation(
            summary = "이메일 인증 코드 전송",
            description = "가입되지 않은 이메일로 6자리 인증 코드를 전송합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 코드 전송 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 이메일 형식")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "인증 이메일 전송 실패")
    @PostMapping("/email-verifications/send")
    ResponseEntity<ApiResponse<Void>> sendEmailVerification(
            @Valid @RequestBody EmailVerificationSendRequest request
    );

    // 이메일 인증 코드 확인 API
    @Operation(
            summary = "이메일 인증 코드 확인",
            description = "전송된 인증 코드를 확인하고 이메일 인증 상태를 저장합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 코드 불일치 또는 만료")
    @PostMapping("/email-verifications/confirm")
    ResponseEntity<ApiResponse<Void>> confirmEmailVerification(
            @Valid @RequestBody EmailVerificationConfirmRequest request
    );

    // 이메일 회원가입 API
    @Operation(
            summary = "이메일 회원가입",
            description = "인증이 완료된 이메일과 비밀번호, 닉네임으로 LOCAL 계정을 생성합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "회원가입 입력값 오류")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이메일 인증 필요")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복")
    @PostMapping("/signup")
    ResponseEntity<ApiResponse<Void>> signUp(
            @Valid @RequestBody SignUpRequest request
    );
}
