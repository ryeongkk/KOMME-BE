package com.komme.domain.auth.controller;

import com.komme.common.base.status.SuccessStatus;
import com.komme.common.response.ApiResponse;
import com.komme.domain.auth.controller.docs.OAuthControllerDocs;
import com.komme.domain.auth.dto.request.OAuthAppleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthGoogleLoginRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.service.OAuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/oauth")
@RequiredArgsConstructor
public class OAuthController implements OAuthControllerDocs {

    private final OAuthService oAuthService;

    // Apple 로그인 API
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithApple(
            OAuthAppleLoginRequest request
    ) {
        LoginResponse response = oAuthService.loginWithApple(request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, response);
    }

    // Google 로그인 API
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGoogle(
            OAuthGoogleLoginRequest request
    ) {
        LoginResponse response = oAuthService.loginWithGoogle(request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, response);
    }
}
