package com.komme.domain.user.service;

import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.dto.request.ChangeNicknameRequest;
import com.komme.domain.user.dto.request.ChangePreferredLanguageRequest;
import com.komme.domain.user.dto.request.LocationConsentRequest;
import com.komme.domain.user.dto.response.NicknameAvailabilityResponse;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.exception.UserConstraintExceptionMapper;
import com.komme.domain.user.exception.UserErrorStatus;
import com.komme.domain.user.repository.UserRepository;
import com.komme.domain.i18n.enums.Language;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTests {

    private static final Long USER_ID = 1L;

    @Mock
    private UserReader userReader;

    @Mock
    private UserRepository userRepository;

    private UserProfileService userProfileService;

    // 사용자 프로필 서비스 테스트 환경 구성
    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService(
                userReader,
                userRepository,
                new UserConstraintExceptionMapper()
        );
    }

    // 마이페이지 프로필 조회 응답 검증
    @Test
    void getMyProfileReturnsUserProfile() {
        User user = mock(User.class);
        when(user.getNickname()).thenReturn("nickname");
        when(user.getProvider()).thenReturn(Provider.LOCAL);
        when(user.getPreferredLanguage()).thenReturn(Language.JAPANESE);
        when(user.isLocationConsentAgreed()).thenReturn(true);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);

        UserProfileResponse response = userProfileService.getMyProfile(USER_ID);

        assertThat(response.nickname()).isEqualTo("nickname");
        assertThat(response.provider()).isEqualTo(Provider.LOCAL);
        assertThat(response.preferredLanguage()).isEqualTo(Language.JAPANESE);
        assertThat(response.locationConsentAgreed()).isTrue();
    }

    // 닉네임 변경 검증
    @Test
    void changeNicknameUpdatesTrimmedNickname() {
        User user = mock(User.class);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);

        userProfileService.changeNickname(
                USER_ID,
                new ChangeNicknameRequest(" new-nickname ")
        );

        verify(user).changeNickname("new-nickname");
        verify(userRepository).flush();
    }

    // 본인 제외 닉네임 중복 거부 검증
    @Test
    void changeNicknameRejectsDuplicateNicknameFromOtherUser() {
        when(userRepository.existsByNicknameAndIdNot("nickname", USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> userProfileService.changeNickname(
                USER_ID,
                new ChangeNicknameRequest("nickname")
        ))
                .isInstanceOf(com.komme.common.exception.GeneralException.class)
                .extracting(exception -> ((com.komme.common.exception.GeneralException) exception).getErrorStatus())
                .isEqualTo(UserErrorStatus.NICKNAME_ALREADY_EXISTS);

        verify(userReader, never()).findByIdOrThrow(USER_ID);
        verify(userRepository, never()).flush();
    }

    // 닉네임 unique 제약조건 오류 변환 검증
    @Test
    void changeNicknameMapsNicknameUniqueConstraintViolation() {
        User user = mock(User.class);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);
        doThrow(createUniqueConstraintException()).when(userRepository).flush();

        assertThatThrownBy(() -> userProfileService.changeNickname(
                USER_ID,
                new ChangeNicknameRequest("nickname")
        ))
                .isInstanceOf(com.komme.common.exception.GeneralException.class)
                .extracting(exception -> ((com.komme.common.exception.GeneralException) exception).getErrorStatus())
                .isEqualTo(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
    }

    // 닉네임 사용 가능 여부 조회 가능 검증
    @Test
    void getNicknameAvailabilityReturnsTrueWhenNicknameDoesNotExist() {
        when(userReader.existsByNickname("nickname")).thenReturn(false);

        NicknameAvailabilityResponse response =
                userProfileService.getNicknameAvailability("nickname");

        assertThat(response.available()).isTrue();
    }

    // 닉네임 사용 가능 여부 조회 불가 검증
    @Test
    void getNicknameAvailabilityReturnsFalseWhenNicknameExists() {
        when(userReader.existsByNickname("nickname")).thenReturn(true);

        NicknameAvailabilityResponse response =
                userProfileService.getNicknameAvailability("nickname");

        assertThat(response.available()).isFalse();
    }

    // 선호 언어 변경 검증
    @Test
    void changePreferredLanguageUpdatesPreferredLanguage() {
        User user = mock(User.class);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);

        userProfileService.changePreferredLanguage(
                USER_ID,
                new ChangePreferredLanguageRequest(Language.ENGLISH)
        );

        verify(user).changePreferredLanguage(Language.ENGLISH);
    }

    // 위치 정보 동의 상태 변경 검증
    @Test
    void updateLocationConsentUpdatesUserAgreement() {
        User user = mock(User.class);
        when(userReader.findByIdOrThrow(USER_ID)).thenReturn(user);

        userProfileService.updateLocationConsent(
                USER_ID,
                new LocationConsentRequest(true)
        );

        verify(user).changeLocationConsent(true);
    }

    // Hibernate unique 제약조건 예외 생성
    private DataIntegrityViolationException createUniqueConstraintException() {
        ConstraintViolationException cause = mock(ConstraintViolationException.class);
        when(cause.getConstraintName()).thenReturn("uk_user_nickname");
        return new DataIntegrityViolationException("unique constraint", cause);
    }
}
