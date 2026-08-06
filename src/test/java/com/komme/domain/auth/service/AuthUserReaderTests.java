package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.service.UserReader;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserReaderTests {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@example.com";

    @Mock
    private UserReader userReader;

    private AuthUserReader authUserReader;

    // 인증 사용자 Reader 테스트 환경 구성
    @BeforeEach
    void setUp() {
        authUserReader = new AuthUserReader(userReader);
    }

    // LOCAL 이메일 사용자 조회 성공 검증
    @Test
    void findLocalByEmailOrThrowReturnsLocalUser() {
        User user = createUser(Provider.LOCAL);
        when(userReader.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        User foundUser = authUserReader.findLocalByEmailOrThrow(EMAIL);

        assertThat(foundUser).isSameAs(user);
    }

    // LOCAL 이메일 사용자 미존재 오류 검증
    @Test
    void findLocalByEmailOrThrowRejectsMissingUser() {
        when(userReader.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authUserReader.findLocalByEmailOrThrow(EMAIL))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_CREDENTIALS);
    }

    // OAuth 이메일 사용자 LOCAL 조회 거부 검증
    @Test
    void findLocalByEmailOrThrowRejectsOAuthUser() {
        User user = createUser(Provider.GOOGLE);
        when(userReader.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authUserReader.findLocalByEmailOrThrow(EMAIL))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_CREDENTIALS);
    }

    // 비밀번호 재설정 LOCAL 사용자 조회 성공 검증
    @Test
    void findLocalByEmailForPasswordResetReturnsLocalUser() {
        User user = createUser(Provider.LOCAL);
        when(userReader.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Optional<User> foundUser = authUserReader.findLocalByEmailForPasswordReset(EMAIL);

        assertThat(foundUser).contains(user);
    }

    // 비밀번호 재설정 LOCAL 사용자 미존재 오류 검증
    @Test
    void findLocalByEmailForPasswordResetOrThrowRejectsMissingLocalUser() {
        User user = createUser(Provider.APPLE);
        when(userReader.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authUserReader.findLocalByEmailForPasswordResetOrThrow(EMAIL))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_NOT_REGISTERED);
    }

    // LOCAL ID 사용자 조회 성공 검증
    @Test
    void findLocalByIdOrThrowReturnsLocalUser() {
        User user = createUser(Provider.LOCAL);
        when(userReader.findById(USER_ID)).thenReturn(Optional.of(user));

        User foundUser = authUserReader.findLocalByIdOrThrow(USER_ID);

        assertThat(foundUser).isSameAs(user);
    }

    // LOCAL ID 사용자 조회 토큰 오류 검증
    @Test
    void findLocalByIdOrThrowRejectsMissingUser() {
        when(userReader.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authUserReader.findLocalByIdOrThrow(USER_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // 테스트 사용자 Mock 생성
    private User createUser(Provider provider) {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getProvider()).thenReturn(provider);
        return user;
    }
}
