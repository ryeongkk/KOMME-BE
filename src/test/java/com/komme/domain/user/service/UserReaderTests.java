package com.komme.domain.user.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.exception.UserErrorStatus;
import com.komme.domain.user.repository.UserRepository;

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
class UserReaderTests {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@example.com";
    private static final String NICKNAME = "nickname";

    @Mock
    private UserRepository userRepository;

    private UserReader userReader;

    // 사용자 Reader 테스트 환경 구성
    @BeforeEach
    void setUp() {
        userReader = new UserReader(userRepository);
    }

    // ID 기반 조회 위임 검증
    @Test
    void findByIdDelegatesToRepository() {
        User user = User.createOAuth(EMAIL, Provider.GOOGLE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThat(userReader.findById(USER_ID)).contains(user);
    }

    // 이메일 기반 조회 위임 검증
    @Test
    void findByEmailDelegatesToRepository() {
        User user = User.createOAuth(EMAIL, Provider.GOOGLE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThat(userReader.findByEmail(EMAIL)).contains(user);
    }

    // ID 필수 조회 성공 검증
    @Test
    void findByIdOrThrowReturnsUserWhenFound() {
        User user = User.createOAuth(EMAIL, Provider.GOOGLE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThat(userReader.findByIdOrThrow(USER_ID)).isEqualTo(user);
    }

    // ID 필수 조회 실패 시 USER_NOT_FOUND 예외 검증
    @Test
    void findByIdOrThrowThrowsWhenNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userReader.findByIdOrThrow(USER_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(UserErrorStatus.USER_NOT_FOUND);
    }

    // ID 존재 여부 조회 위임 검증
    @Test
    void existsByIdDelegatesToRepository() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        assertThat(userReader.existsById(USER_ID)).isTrue();
    }

    // 닉네임 존재 여부 조회 위임 검증
    @Test
    void existsByNicknameDelegatesToRepository() {
        when(userRepository.existsByNickname(NICKNAME)).thenReturn(true);

        boolean exists = userReader.existsByNickname(NICKNAME);

        assertThat(exists).isTrue();
    }
}
