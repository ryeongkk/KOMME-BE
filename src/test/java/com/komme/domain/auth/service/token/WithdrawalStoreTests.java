package com.komme.domain.auth.service.token;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalStoreTests {

    private static final String EMAIL = "user@example.com";
    private static final String KEY = "withdrawn:email:" + EMAIL;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // 탈퇴 이메일 Redis 등록 검증
    @Test
    void markWithdrawnStoresEmailWithGracePeriod() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        createStore().markWithdrawn(EMAIL);

        verify(valueOperations).set(KEY, "true", Duration.ofDays(7));
    }

    // 탈퇴 유예기간 이메일 거부 검증
    @Test
    void validateNotWithdrawnRejectsWithdrawnEmail() {
        when(redisTemplate.hasKey(KEY)).thenReturn(true);

        assertThatThrownBy(() -> createStore().validateNotWithdrawn(EMAIL))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.WITHDRAWAL_GRACE_PERIOD);
    }

    // 탈퇴 유예기간 미등록 이메일 통과 검증
    @Test
    void validateNotWithdrawnAllowsUnknownEmail() {
        when(redisTemplate.hasKey(KEY)).thenReturn(false);

        createStore().validateNotWithdrawn(EMAIL);
    }

    // 탈퇴 Store 생성
    private WithdrawalStore createStore() {
        return new WithdrawalStore(redisTemplate);
    }
}
