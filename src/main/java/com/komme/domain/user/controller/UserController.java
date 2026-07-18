package com.komme.domain.user.controller;

import com.komme.common.base.status.SuccessStatus;
import com.komme.common.response.ApiResponse;
import com.komme.domain.auth.enums.TermsType;
import com.komme.domain.user.controller.docs.UserControllerDocs;
import com.komme.domain.user.dto.request.ChangeNicknameRequest;
import com.komme.domain.user.dto.request.ChangePreferredLanguageRequest;
import com.komme.domain.user.dto.request.UpdateTermsAgreementRequest;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.service.UserProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserProfileService userProfileService;

    // 마이페이지 프로필 조회 API
    @Override
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Long userId) {
        UserProfileResponse response = userProfileService.getMyProfile(userId);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, response);
    }

    // 마이페이지 닉네임 변경 API
    @Override
    public ResponseEntity<ApiResponse<Void>> changeNickname(
            Long userId,
            ChangeNicknameRequest request
    ) {
        userProfileService.changeNickname(userId, request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }

    // 마이페이지 선호 언어 변경 API
    @Override
    public ResponseEntity<ApiResponse<Void>> changePreferredLanguage(
            Long userId,
            ChangePreferredLanguageRequest request
    ) {
        userProfileService.changePreferredLanguage(userId, request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }

    // 마이페이지 선택 약관 변경 API
    @Override
    public ResponseEntity<ApiResponse<Void>> updateOptionalTermsAgreement(
            Long userId,
            TermsType termsType,
            UpdateTermsAgreementRequest request
    ) {
        userProfileService.updateOptionalTermsAgreement(userId, termsType, request);
        return ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);
    }
}
