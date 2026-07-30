package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalStore {

    private static final String WITHDRAWN_EMAIL_KEY_PREFIX = "withdrawn:email:";
    private static final String WITHDRAWN_VALUE = "true";
    private static final Duration WITHDRAWAL_GRACE_PERIOD = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    // 탈퇴 이메일 유예기간 등록 기능
    public void markWithdrawn(String email) {
        try {
            redisTemplate.opsForValue().set(
                    withdrawnEmailKey(email),
                    WITHDRAWN_VALUE,
                    WITHDRAWAL_GRACE_PERIOD
            );
        } catch (RuntimeException exception) {
            log.error("Failed to mark withdrawn email in Redis.", exception);
            throw exception;
        }
    }

    // 탈퇴 유예기간 이메일 여부 검증 기능
    public void validateNotWithdrawn(String email) {
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(withdrawnEmailKey(email)))) {
                throw new GeneralException(AuthErrorStatus.WITHDRAWAL_GRACE_PERIOD);
            }
        } catch (GeneralException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("Failed to validate withdrawn email in Redis.", exception);
            throw exception;
        }
    }

    // 탈퇴 이메일 Redis 키 생성 기능
    private String withdrawnEmailKey(String email) {
        return WITHDRAWN_EMAIL_KEY_PREFIX + email;
    }
}
