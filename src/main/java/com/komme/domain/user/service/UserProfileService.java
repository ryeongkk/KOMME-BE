package com.komme.domain.user.service;

import com.komme.domain.auth.service.TermsAgreementService;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.entity.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserReader userReader;
    private final TermsAgreementService termsAgreementService;

    // 사용자 마이페이지 프로필 조회 기능
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userReader.findByIdOrThrow(userId);
        return UserProfileResponse.of(
                user,
                termsAgreementService.getOptionalConsentAgreements(userId)
        );
    }
}
