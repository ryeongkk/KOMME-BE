package com.komme.domain.auth.controller;

import com.komme.common.base.status.SuccessStatus;
import com.komme.common.exception.GeneralException;
import com.komme.common.response.ApiResponse;
import com.komme.domain.auth.controller.docs.AuthControllerDocs;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;
import com.komme.domain.auth.dto.request.TermsAgreementRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.dto.response.TokenReissueResponse;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.service.AuthService;
import com.komme.domain.auth.service.EmailVerificationService;
import com.komme.domain.auth.service.TermsAgreementService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final TermsAgreementService termsAgreementService;

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

    // 이메일 로그인 API
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> login(LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, response);
    }

    // 약관 동의 API
    @Override
    public ResponseEntity<ApiResponse<Void>> agreeTerms(
            Long userId,
            TermsAgreementRequest request
    ) {
        termsAgreementService.agree(userId, request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }

    // 토큰 재발급 API
    @Override
    public ResponseEntity<ApiResponse<TokenReissueResponse>> reissueToken(
            TokenReissueRequest request
    ) {
        TokenReissueResponse response = authService.reissueToken(request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, response);
    }

    // 비밀번호 변경 API
    @Override
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Long userId,
            PasswordChangeRequest request
    ) {
        authService.changePassword(userId, request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }

    // 로그아웃 API
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
            Long userId,
            Authentication authentication,
            LogoutRequest request
    ) {
        TokenClaims tokenClaims = resolveTokenClaims(authentication);
        authService.logout(userId, tokenClaims, request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }

    // Authentication JWT 세부정보 조회 기능
    private TokenClaims resolveTokenClaims(Authentication authentication) {
        if (authentication.getDetails() instanceof TokenClaims tokenClaims) {
            return tokenClaims;
        }

        throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
    }
}
