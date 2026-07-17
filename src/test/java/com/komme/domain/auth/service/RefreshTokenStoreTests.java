package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.jwt.JwtRedisKeys;
import com.komme.domain.auth.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenStoreTests {

    private static final Long USER_ID = 1L;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    // Refresh Token 저장값 불일치 거부 검증
    @Test
    void validateAndConsumeRejectsInvalidStoredUser() {
        prepareValueOperations();
        prepareSetOperations();
        TokenClaims claims = createClaims("refresh-id");
        when(valueOperations.getAndDelete(JwtRedisKeys.refreshToken("refresh-id")))
                .thenReturn("different-user");

        assertThatThrownBy(() -> createStore().validateAndConsume(claims))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // 사용자 전체 Refresh Token 삭제 검증
    @Test
    @SuppressWarnings("unchecked")
    void invalidateAllDeletesTokenKeys() {
        prepareSetOperations();
        when(setOperations.members(JwtRedisKeys.userRefreshTokens(USER_ID)))
                .thenReturn(Set.of("refresh-id-1", "refresh-id-2"));

        createStore().invalidateAll(USER_ID);

        ArgumentCaptor<Collection<String>> keysCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(redisTemplate).delete(keysCaptor.capture());
        verify(redisTemplate).delete(JwtRedisKeys.userRefreshTokens(USER_ID));
    }

    // Refresh Token Store 생성
    private RefreshTokenStore createStore() {
        return new RefreshTokenStore(redisTemplate, userRepository);
    }

    // Refresh Token 테스트 Claim 생성
    private TokenClaims createClaims(String tokenId) {
        return new TokenClaims(
                USER_ID,
                tokenId,
                Instant.now().plus(Duration.ofHours(1))
        );
    }

    // Redis 문자열 연산 Mock 구성
    private void prepareValueOperations() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // Redis Set 연산 Mock 구성
    private void prepareSetOperations() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }
}
