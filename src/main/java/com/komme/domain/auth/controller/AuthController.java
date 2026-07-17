package com.komme.domain.auth.controller;

import com.komme.common.base.status.SuccessStatus;
import com.komme.common.response.ApiResponse;
import com.komme.domain.auth.controller.docs.AuthControllerDocs;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.service.AuthService;
import com.komme.domain.auth.service.EmailVerificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    // 이메일 인증 코드 전송 API
    @Override
    public ResponseEntity<ApiResponse<Void>> sendEmailVerification(
            EmailVerificationSendRequest request
    ) {
        emailVerificationService.sendVerificationCode(request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }

    // 이메일 인증 코드 확인 API
    @Override
    public ResponseEntity<ApiResponse<Void>> confirmEmailVerification(
            EmailVerificationConfirmRequest request
    ) {
        emailVerificationService.confirmVerificationCode(request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }

    // 이메일 회원가입 API
    @Override
    public ResponseEntity<ApiResponse<Void>> signUp(SignUpRequest request) {
        authService.signUp(request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }
}
