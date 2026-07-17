package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.AuthMailProperties;
import com.komme.domain.auth.properties.EmailVerificationProperties;
import com.komme.domain.auth.repository.UserRepository;
import com.komme.domain.auth.util.EmailNormalizer;

import java.security.SecureRandom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String VERIFICATION_CODE_KEY_PREFIX = "auth:email-verification:code:";
    private static final String VERIFIED_EMAIL_KEY_PREFIX = "auth:email-verification:verified:";
    private static final String VERIFICATION_ATTEMPT_KEY_PREFIX = "auth:email-verification:attempt:";
    private static final String VERIFICATION_LOCK_KEY_PREFIX = "auth:email-verification:lock:";
    private static final String VERIFICATION_COOLDOWN_KEY_PREFIX = "auth:email-verification:cooldown:";
    private static final String VERIFIED_EMAIL_VALUE = "true";
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final AuthMailProperties mailProperties;
    private final EmailVerificationProperties emailVerificationProperties;

    // 이메일 인증 코드 전송 및 Redis 저장 기능
    public void sendVerificationCode(EmailVerificationSendRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        validateEmailNotRegistered(email);
        validateEmailNotLocked(email);
        acquireSendCooldown(email);
        redisTemplate.delete(createVerificationAttemptKey(email));

        String verificationCode = generateVerificationCode();
        String redisKey = createVerificationCodeKey(email);
        redisTemplate.opsForValue().set(
                redisKey,
                verificationCode,
                emailVerificationProperties.getCodeExpiration()
        );

        try {
            sendVerificationEmail(email, verificationCode);
        } catch (MailException exception) {
            redisTemplate.delete(redisKey);
            redisTemplate.delete(createVerificationCooldownKey(email));
            throw new GeneralException(AuthErrorStatus.EMAIL_SEND_FAILED, exception);
        }
    }

    // 이메일 인증 코드 확인 및 인증 완료 플래그 저장 기능
    public void confirmVerificationCode(EmailVerificationConfirmRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        validateEmailNotLocked(email);
        String verificationCodeKey = createVerificationCodeKey(email);
        String savedVerificationCode = redisTemplate.opsForValue().get(verificationCodeKey);

        if (savedVerificationCode == null) {
            throw new GeneralException(AuthErrorStatus.EXPIRED_VERIFICATION_CODE);
        }

        if (!savedVerificationCode.equals(request.verificationCode())) {
            recordFailedAttempt(email, verificationCodeKey);
            throw new GeneralException(AuthErrorStatus.INVALID_VERIFICATION_CODE);
        }

        redisTemplate.opsForValue().set(
                createVerifiedEmailKey(email),
                VERIFIED_EMAIL_VALUE,
                emailVerificationProperties.getVerifiedExpiration()
        );
        redisTemplate.delete(verificationCodeKey);
        redisTemplate.delete(createVerificationAttemptKey(email));
    }

    // 이메일 인증 완료 여부 검증 기능
    public void validateVerifiedEmail(String email) {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(createVerifiedEmailKey(email)))) {
            throw new GeneralException(AuthErrorStatus.EMAIL_NOT_VERIFIED);
        }
    }

    // 이메일 인증 완료 플래그 삭제 기능
    public void deleteVerifiedEmail(String email) {
        redisTemplate.delete(createVerifiedEmailKey(email));
    }

    // 가입된 이메일 여부 확인 기능
    private void validateEmailNotRegistered(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
        }
    }

    // 이메일 인증 잠금 여부 검증 기능
    private void validateEmailNotLocked(String email) {
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
            redisTemplate.delete(verificationCodeKey);
            redisTemplate.delete(attemptKey);
            redisTemplate.opsForValue().set(
                    createVerificationLockKey(email),
                    "true",
                    emailVerificationProperties.getLockExpiration()
            );
            throw new GeneralException(AuthErrorStatus.EMAIL_VERIFICATION_LOCKED);
        }
    }

    // 보안 난수 기반 6자리 인증 코드 생성
    private String generateVerificationCode() {
        return "%06d".formatted(secureRandom.nextInt(VERIFICATION_CODE_BOUND));
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

    // Gmail SMTP 인증 코드 전송 기능
    private void sendVerificationEmail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getSender());
        message.setTo(email);
        message.setSubject("[KOMME] 이메일 인증 코드");
        message.setText(
                "이메일 인증 코드는 " + verificationCode + "입니다. "
                        + emailVerificationProperties.getCodeExpiration().toMinutes()
                        + "분 안에 입력해 주세요."
        );
        mailSender.send(message);
    }
}
