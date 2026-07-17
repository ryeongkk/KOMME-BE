package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.EmailVerificationProperties;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerificationStore {

    private static final String VERIFICATION_CODE_KEY_PREFIX = "auth:email-verification:code:";
    private static final String VERIFIED_EMAIL_KEY_PREFIX = "auth:email-verification:verified:";
    private static final String VERIFICATION_ATTEMPT_KEY_PREFIX = "auth:email-verification:attempt:";
    private static final String VERIFICATION_LOCK_KEY_PREFIX = "auth:email-verification:lock:";
    private static final String VERIFICATION_COOLDOWN_KEY_PREFIX = "auth:email-verification:cooldown:";
    private static final String VERIFIED_EMAIL_VALUE = "true";

    private final StringRedisTemplate redisTemplate;
    private final EmailVerificationProperties emailVerificationProperties;

    // 인증 코드 전송 가능 상태 준비 기능
    public void prepareSend(String email) {
        validateNotLocked(email);
        acquireSendCooldown(email);
        redisTemplate.delete(createVerificationAttemptKey(email));
    }

    // 이메일 인증 코드 TTL 저장 기능
    public void saveCode(String email, String verificationCode) {
        redisTemplate.opsForValue().set(
                createVerificationCodeKey(email),
                verificationCode,
                emailVerificationProperties.getCodeExpiration()
        );
    }

    // 이메일 인증 코드 확인 및 인증 완료 저장 기능
    public void confirmCode(String email, String verificationCode) {
        validateNotLocked(email);
        String verificationCodeKey = createVerificationCodeKey(email);
        String savedVerificationCode = redisTemplate.opsForValue().get(verificationCodeKey);

        if (savedVerificationCode == null) {
            throw new GeneralException(AuthErrorStatus.EXPIRED_VERIFICATION_CODE);
        }

        if (!savedVerificationCode.equals(verificationCode)) {
            recordFailedAttempt(email, verificationCodeKey);
            throw new GeneralException(AuthErrorStatus.INVALID_VERIFICATION_CODE);
        }

        markVerified(email, verificationCodeKey);
    }

    // 이메일 인증 완료 여부 검증 기능
    public void validateVerified(String email) {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(createVerifiedEmailKey(email)))) {
            throw new GeneralException(AuthErrorStatus.EMAIL_NOT_VERIFIED);
        }
    }

    // 이메일 인증 완료 플래그 삭제 기능
    public void deleteVerified(String email) {
        redisTemplate.delete(createVerifiedEmailKey(email));
    }

    // 인증 이메일 발송 실패 데이터 정리 기능
    public void rollbackSend(String email) {
        redisTemplate.delete(createVerificationCodeKey(email));
        redisTemplate.delete(createVerificationCooldownKey(email));
    }

    // 이메일 인증 잠금 여부 검증 기능
    private void validateNotLocked(String email) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(createVerificationLockKey(email)))) {
            throw new GeneralException(AuthErrorStatus.EMAIL_VERIFICATION_LOCKED);
        }
    }

    // 이메일 인증 코드 재전송 cooldown 획득 기능
    private void acquireSendCooldown(String email) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                createVerificationCooldownKey(email),
                "true",
                emailVerificationProperties.getResendCooldown()
        );

        if (!Boolean.TRUE.equals(acquired)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_SEND_TOO_FREQUENTLY);
        }
    }

    // 이메일 인증 코드 실패 횟수 기록 및 잠금 기능
    private void recordFailedAttempt(String email, String verificationCodeKey) {
        String attemptKey = createVerificationAttemptKey(email);
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        if (Long.valueOf(1L).equals(attempts)) {
            redisTemplate.expire(
                    attemptKey,
                    emailVerificationProperties.getCodeExpiration()
            );
        }

        if (attempts != null && attempts >= emailVerificationProperties.getMaxAttempts()) {
            lockEmail(email, verificationCodeKey, attemptKey);
        }
    }

    // 이메일 인증 코드 실패 횟수 초과 잠금 기능
    private void lockEmail(String email, String verificationCodeKey, String attemptKey) {
        redisTemplate.delete(verificationCodeKey);
        redisTemplate.delete(attemptKey);
        redisTemplate.opsForValue().set(
                createVerificationLockKey(email),
                "true",
                emailVerificationProperties.getLockExpiration()
        );
        throw new GeneralException(AuthErrorStatus.EMAIL_VERIFICATION_LOCKED);
    }

    // 이메일 인증 완료 상태 저장 기능
    private void markVerified(String email, String verificationCodeKey) {
        redisTemplate.opsForValue().set(
                createVerifiedEmailKey(email),
                VERIFIED_EMAIL_VALUE,
                emailVerificationProperties.getVerifiedExpiration()
        );
        redisTemplate.delete(verificationCodeKey);
        redisTemplate.delete(createVerificationAttemptKey(email));
    }

    // 이메일별 인증 코드 Redis 키 생성
    private String createVerificationCodeKey(String email) {
        return VERIFICATION_CODE_KEY_PREFIX + email;
    }

    // 이메일별 인증 완료 Redis 키 생성
    private String createVerifiedEmailKey(String email) {
        return VERIFIED_EMAIL_KEY_PREFIX + email;
    }

    // 이메일별 인증 코드 실패 횟수 Redis 키 생성
    private String createVerificationAttemptKey(String email) {
        return VERIFICATION_ATTEMPT_KEY_PREFIX + email;
    }

    // 이메일별 인증 잠금 Redis 키 생성
    private String createVerificationLockKey(String email) {
        return VERIFICATION_LOCK_KEY_PREFIX + email;
    }

    // 이메일별 인증 코드 재전송 cooldown Redis 키 생성
    private String createVerificationCooldownKey(String email) {
        return VERIFICATION_COOLDOWN_KEY_PREFIX + email;
    }
}
