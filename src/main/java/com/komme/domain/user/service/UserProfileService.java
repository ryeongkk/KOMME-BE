package com.komme.domain.user.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.service.AuthConstraintExceptionMapper;
import com.komme.domain.auth.service.TermsAgreementService;
import com.komme.domain.user.dto.request.ChangeNicknameRequest;
import com.komme.domain.user.dto.request.ChangePreferredLanguageRequest;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserReader userReader;
    private final UserRepository userRepository;
    private final TermsAgreementService termsAgreementService;
    private final AuthConstraintExceptionMapper authConstraintExceptionMapper;

    // 사용자 마이페이지 프로필 조회 기능
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userReader.findByIdOrThrow(userId);
        return UserProfileResponse.of(
                user,
                termsAgreementService.getOptionalConsentAgreements(userId)
        );
    }

    // 사용자 닉네임 변경 기능
    @Transactional
    public void changeNickname(Long userId, ChangeNicknameRequest request) {
        String nickname = request.nickname().trim();
        validateNicknameNotUsedByOthers(userId, nickname);

        User user = userReader.findByIdOrThrow(userId);
        user.changeNickname(nickname);
        flushUserChanges(AuthErrorStatus.NICKNAME_ALREADY_EXISTS);
    }

    // 사용자 선호 언어 변경 기능
    @Transactional
    public void changePreferredLanguage(
            Long userId,
            ChangePreferredLanguageRequest request
    ) {
        User user = userReader.findByIdOrThrow(userId);
        user.changePreferredLanguage(request.preferredLanguage());
    }

    // 본인 제외 닉네임 중복 검증 기능
    private void validateNicknameNotUsedByOthers(Long userId, String nickname) {
        if (userReader.existsByNicknameAndIdNot(nickname, userId)) {
            throw new GeneralException(AuthErrorStatus.NICKNAME_ALREADY_EXISTS);
        }
    }

    // 사용자 변경사항 flush 및 unique 오류 변환 기능
    private void flushUserChanges(AuthErrorStatus defaultStatus) {
        try {
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw authConstraintExceptionMapper.map(exception, defaultStatus);
        }
    }
}
