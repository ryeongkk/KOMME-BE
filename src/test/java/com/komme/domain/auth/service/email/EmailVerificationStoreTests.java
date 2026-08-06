package com.komme.domain.auth.service.email;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.enums.EmailVerificationPurpose;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.EmailVerificationProperties;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
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
class EmailVerificationStoreTests {

    private static final String EMAIL = "user@example.com";
    private static final String CODE_KEY = "auth:email-verification:code:SIGN_UP:" + EMAIL;
    private static final String PASSWORD_RESET_CODE_KEY =
            "auth:email-verification:code:PASSWORD_RESET:" + EMAIL;
    private static final String VERIFIED_KEY = "auth:email-verification:verified:" + EMAIL;
    private static final String ATTEMPT_KEY = "auth:email-verification:attempt:SIGN_UP:" + EMAIL;
    private static final String PASSWORD_RESET_ATTEMPT_KEY =
            "auth:email-verification:attempt:PASSWORD_RESET:" + EMAIL;
    private static final String LOCK_KEY = "auth:email-verification:lock:SIGN_UP:" + EMAIL;
    private static final String COOLDOWN_KEY =
            "auth:email-verification:cooldown:SIGN_UP:" + EMAIL;
    private static final String RESET_TOKEN_KEY = "auth:password-reset:token:reset-token";
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);
    private static final Duration VERIFIED_EXPIRATION = Duration.ofMinutes(30);
    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
    private static final Duration LOCK_EXPIRATION = Duration.ofMinutes(5);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private EmailVerificationStore emailVerificationStore;

    // 이메일 인증 Store 테스트 환경 구성
    @BeforeEach
    void setUp() {
        emailVerificationStore = new EmailVerificationStore(
                redisTemplate,
                new EmailVerificationProperties(
                        CODE_EXPIRATION,
                        VERIFIED_EXPIRATION,
                        5,
                        RESEND_COOLDOWN,
                        LOCK_EXPIRATION
                )
        );
    }

    // 인증 코드 전송 준비와 실패 횟수 초기화 검증
    @Test
    void prepareSendAcquiresCooldownAndClearsAttempts() {
        prepareValueOperations();
        when(valueOperations.setIfAbsent(COOLDOWN_KEY, "true", RESEND_COOLDOWN))
                .thenReturn(true);

        emailVerificationStore.prepareSend(EMAIL);

        verify(redisTemplate).delete(ATTEMPT_KEY);
    }

    // 인증 코드 재전송 cooldown 거부 검증
    @Test
    void prepareSendRejectsCooldownRequest() {
        prepareValueOperations();
        when(valueOperations.setIfAbsent(COOLDOWN_KEY, "true", RESEND_COOLDOWN))
                .thenReturn(false);

        assertThatThrownBy(() -> emailVerificationStore.prepareSend(EMAIL))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_SEND_TOO_FREQUENTLY);
    }

    // 인증 코드 TTL 저장 검증
    @Test
    void saveCodeStoresCodeWithExpiration() {
        prepareValueOperations();

        emailVerificationStore.saveCode(EMAIL, "123456");

        verify(valueOperations).set(CODE_KEY, "123456", CODE_EXPIRATION);
    }

    // 목적별 인증 코드 TTL 저장 검증
    @Test
    void saveCodeStoresPurposeScopedCodeWithExpiration() {
        prepareValueOperations();

        emailVerificationStore.saveCode(
                EmailVerificationPurpose.PASSWORD_RESET,
                EMAIL,
                "123456"
        );

        verify(valueOperations).set(PASSWORD_RESET_CODE_KEY, "123456", CODE_EXPIRATION);
    }

    // 인증 코드 확인 성공과 인증 상태 저장 검증
    @Test
    void confirmCodeStoresVerifiedFlag() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn("123456");

        emailVerificationStore.confirmCode(EMAIL, "123456");

        verify(valueOperations).set(VERIFIED_KEY, "true", VERIFIED_EXPIRATION);
        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(ATTEMPT_KEY);
    }

    // 만료된 인증 코드 거부 검증
    @Test
    void confirmCodeRejectsExpiredCode() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn(null);

        assertThatThrownBy(() -> emailVerificationStore.confirmCode(EMAIL, "123456"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EXPIRED_VERIFICATION_CODE);
    }

    // 인증 코드 불일치 실패 횟수 기록 검증
    @Test
    void confirmCodeRecordsFailedAttempt() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn("654321");
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn(1L);

        assertThatThrownBy(() -> emailVerificationStore.confirmCode(EMAIL, "123456"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_VERIFICATION_CODE);

        verify(redisTemplate).expire(ATTEMPT_KEY, CODE_EXPIRATION);
    }

    // 인증 코드 최대 실패 횟수 잠금 검증
    @Test
    void confirmCodeLocksEmailAfterMaxAttempts() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn("654321");
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn(5L);

        assertThatThrownBy(() -> emailVerificationStore.confirmCode(EMAIL, "123456"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_VERIFICATION_LOCKED);

        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(ATTEMPT_KEY);
        verify(valueOperations).set(LOCK_KEY, "true", LOCK_EXPIRATION);
    }

    // 인증 이메일 발송 실패 코드와 cooldown 삭제 검증
    @Test
    void rollbackSendDeletesCodeAndCooldown() {
        emailVerificationStore.rollbackSend(EMAIL);

        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(COOLDOWN_KEY);
    }

    // 비밀번호 재설정 코드 확인 성공 시 코드와 실패 횟수 삭제 검증
    @Test
    void confirmPasswordResetCodeDeletesCodeAndAttempts() {
        prepareValueOperations();
        when(valueOperations.get(PASSWORD_RESET_CODE_KEY)).thenReturn("123456");

        emailVerificationStore.confirmPasswordResetCode(EMAIL, "123456");

        verify(redisTemplate).delete(PASSWORD_RESET_CODE_KEY);
        verify(redisTemplate).delete(PASSWORD_RESET_ATTEMPT_KEY);
    }

    // 만료된 비밀번호 재설정 코드 거부 검증
    @Test
    void confirmPasswordResetCodeRejectsExpiredCode() {
        prepareValueOperations();
        when(valueOperations.get(PASSWORD_RESET_CODE_KEY)).thenReturn(null);

        assertThatThrownBy(() -> emailVerificationStore.confirmPasswordResetCode(EMAIL, "123456"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EXPIRED_VERIFICATION_CODE);
    }

    // 비밀번호 재설정 코드 불일치 시 실패 횟수 기록 검증
    @Test
    void confirmPasswordResetCodeRecordsFailedAttempt() {
        prepareValueOperations();
        when(valueOperations.get(PASSWORD_RESET_CODE_KEY)).thenReturn("654321");
        when(valueOperations.increment(PASSWORD_RESET_ATTEMPT_KEY)).thenReturn(1L);

        assertThatThrownBy(() -> emailVerificationStore.confirmPasswordResetCode(EMAIL, "123456"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_VERIFICATION_CODE);

        verify(redisTemplate).expire(PASSWORD_RESET_ATTEMPT_KEY, CODE_EXPIRATION);
    }

    // 이미 잠긴 이메일이면 인증 코드 전송/확인 자체를 거부하는지 검증 (validateNotLocked 공통 분기)
    @Test
    void prepareSendRejectsWhenAlreadyLocked() {
        when(redisTemplate.hasKey(LOCK_KEY)).thenReturn(true);

        assertThatThrownBy(() -> emailVerificationStore.prepareSend(EMAIL))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_VERIFICATION_LOCKED);
    }

    // Redis increment가 null을 반환해도(방어적 null 체크) 잠기지 않고 넘어가는지 검증
    @Test
    void confirmCodeDoesNotLockWhenIncrementReturnsNull() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn("654321");
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn(null);

        assertThatThrownBy(() -> emailVerificationStore.confirmCode(EMAIL, "123456"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_VERIFICATION_CODE);
    }

    // 비밀번호 재설정 토큰 TTL 저장 검증
    @Test
    void savePasswordResetTokenStoresEmailWithExpiration() {
        prepareValueOperations();

        emailVerificationStore.savePasswordResetToken("reset-token", EMAIL);

        verify(valueOperations).set(RESET_TOKEN_KEY, EMAIL, VERIFIED_EXPIRATION);
    }

    // 비밀번호 재설정 토큰 소비 검증
    @Test
    void consumePasswordResetTokenReturnsEmail() {
        prepareValueOperations();
        when(valueOperations.getAndDelete(RESET_TOKEN_KEY)).thenReturn(EMAIL);

        String email = emailVerificationStore.consumePasswordResetToken("reset-token");

        org.assertj.core.api.Assertions.assertThat(email).isEqualTo(EMAIL);
    }

    // 유효하지 않은 비밀번호 재설정 토큰 거부 검증
    @Test
    void consumePasswordResetTokenRejectsInvalidToken() {
        prepareValueOperations();
        when(valueOperations.getAndDelete(RESET_TOKEN_KEY)).thenReturn(null);

        assertThatThrownBy(() -> emailVerificationStore.consumePasswordResetToken("reset-token"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_RESET_TOKEN);
    }

    // 미인증 이메일 거부 검증
    @Test
    void validateVerifiedRejectsUnverifiedEmail() {
        when(redisTemplate.hasKey(VERIFIED_KEY)).thenReturn(false);

        assertThatThrownBy(() -> emailVerificationStore.validateVerified(EMAIL))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_NOT_VERIFIED);
    }

    // 인증된 이메일이면 예외 없이 통과하는지 검증
    @Test
    void validateVerifiedPassesWhenEmailIsVerified() {
        when(redisTemplate.hasKey(VERIFIED_KEY)).thenReturn(true);

        org.assertj.core.api.Assertions.assertThatCode(() -> emailVerificationStore.validateVerified(EMAIL))
                .doesNotThrowAnyException();
    }

    // 인증 완료 플래그 삭제 검증
    @Test
    void deleteVerifiedDeletesVerifiedFlag() {
        emailVerificationStore.deleteVerified(EMAIL);

        verify(redisTemplate).delete(VERIFIED_KEY);
    }

    // Redis 문자열 연산 Mock 구성
    private void prepareValueOperations() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
}
