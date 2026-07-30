package com.komme.domain.user.service;

import com.komme.domain.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReaderTests {

    private static final String NICKNAME = "nickname";

    @Mock
    private UserRepository userRepository;

    private UserReader userReader;

    // 사용자 Reader 테스트 환경 구성
    @BeforeEach
    void setUp() {
        userReader = new UserReader(userRepository);
    }

    // 닉네임 존재 여부 조회 위임 검증
    @Test
    void existsByNicknameDelegatesToRepository() {
        when(userRepository.existsByNickname(NICKNAME)).thenReturn(true);

        boolean exists = userReader.existsByNickname(NICKNAME);

        assertThat(exists).isTrue();
    }
}
