package com.komme.domain.user.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.user.enums.TermsType;
import com.komme.domain.user.dto.request.ChangeNicknameRequest;
import com.komme.domain.user.dto.request.ChangePreferredLanguageRequest;
import com.komme.domain.user.dto.request.UpdateTermsAgreementRequest;
import com.komme.domain.user.dto.response.NicknameAvailabilityResponse;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.exception.UserConstraintExceptionMapper;
import com.komme.domain.user.exception.UserErrorStatus;
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
    private final UserConstraintExceptionMapper userConstraintExceptionMapper;

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
        flushUserChanges(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
    }

    // 닉네임 사용 가능 여부 조회 기능
    @Transactional(readOnly = true)
    public NicknameAvailabilityResponse getNicknameAvailability(String nickname) {
        boolean available = !userReader.existsByNickname(nickname.trim());
        return NicknameAvailabilityResponse.of(available);
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

    // 사용자 선택 약관 동의 상태 변경 기능
    public void updateOptionalTermsAgreement(
            Long userId,
            TermsType termsType,
            UpdateTermsAgreementRequest request
    ) {
        termsAgreementService.updateOptionalConsent(userId, termsType, request.agreed());
    }

    // 본인 제외 닉네임 중복 검증 기능
    private void validateNicknameNotUsedByOthers(Long userId, String nickname) {
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new GeneralException(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
        }
    }

    // 사용자 변경사항 flush 및 unique 오류 변환 기능
    private void flushUserChanges(UserErrorStatus defaultStatus) {
        try {
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw userConstraintExceptionMapper.map(exception, defaultStatus);
        }
    }
}
