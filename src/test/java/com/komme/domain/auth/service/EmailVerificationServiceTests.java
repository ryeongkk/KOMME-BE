package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.AuthMailProperties;
import com.komme.domain.auth.properties.EmailVerificationProperties;
import com.komme.domain.auth.repository.UserRepository;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTests {

    private static final String EMAIL = "user@example.com";
    private static final String CODE_KEY = "auth:email-verification:code:" + EMAIL;
    private static final String VERIFIED_KEY = "auth:email-verification:verified:" + EMAIL;
    private static final String ATTEMPT_KEY = "auth:email-verification:attempt:" + EMAIL;
    private static final String LOCK_KEY = "auth:email-verification:lock:" + EMAIL;
    private static final String COOLDOWN_KEY = "auth:email-verification:cooldown:" + EMAIL;
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);
    private static final Duration VERIFIED_EXPIRATION = Duration.ofMinutes(30);

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JavaMailSender mailSender;

    private EmailVerificationService emailVerificationService;

    // 이메일 인증 서비스 테스트 환경 구성
    @BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(
                userRepository,
                redisTemplate,
                mailSender,
                new AuthMailProperties("sender@example.com"),
                new EmailVerificationProperties(
                        CODE_EXPIRATION,
                        VERIFIED_EXPIRATION,
                        5,
                        Duration.ofMinutes(1),
                        Duration.ofMinutes(5)
                )
        );
    }

    // 인증 코드 Redis 저장 및 이메일 전송 검증
    @Test
    void sendVerificationCodeStoresCodeAndSendsEmail() {
        prepareSendOperations();
        EmailVerificationSendRequest request = new EmailVerificationSendRequest(
                "  USER@Example.com  "
        );

        emailVerificationService.sendVerificationCode(request);

        verify(userRepository).existsByEmail(EMAIL);
        verify(valueOperations).set(
                eq(CODE_KEY),
                argThat(code -> code.matches("\\d{6}")),
                eq(CODE_EXPIRATION)
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getTo()).containsExactly(EMAIL);
        assertThat(messageCaptor.getValue().getFrom()).isEqualTo("sender@example.com");
    }

    // 가입된 이메일 인증 코드 전송 거부 검증
    @Test
    void sendVerificationCodeRejectsRegisteredEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest(EMAIL)
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_ALREADY_EXISTS);

        verify(valueOperations, never()).set(any(), any(), any(Duration.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // 이메일 발송 실패 시 Redis 코드 삭제 검증
    @Test
    void sendVerificationCodeDeletesCodeWhenMailFails() {
        prepareSendOperations();
        doThrow(new MailSendException("mail failed"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest(EMAIL)
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_SEND_FAILED);

        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(COOLDOWN_KEY);
    }

    // 재전송 cooldown 중 인증 코드 전송 거부 검증
    @Test
    void sendVerificationCodeRejectsCooldownRequest() {
        prepareValueOperations();
        when(valueOperations.setIfAbsent(
                COOLDOWN_KEY,
                "true",
                Duration.ofMinutes(1)
        )).thenReturn(false);

        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest(EMAIL)
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_SEND_TOO_FREQUENTLY);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // 인증 코드 확인 성공과 인증 완료 플래그 저장 검증
    @Test
    void confirmVerificationCodeStoresVerifiedFlag() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn("123456");

        emailVerificationService.confirmVerificationCode(
                new EmailVerificationConfirmRequest(EMAIL, "123456")
        );

        verify(valueOperations).set(VERIFIED_KEY, "true", VERIFIED_EXPIRATION);
        verify(redisTemplate).delete(CODE_KEY);
    }

    // 만료된 인증 코드 오류 검증
    @Test
    void confirmVerificationCodeRejectsExpiredCode() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn(null);

        assertThatThrownBy(() -> emailVerificationService.confirmVerificationCode(
                new EmailVerificationConfirmRequest(EMAIL, "123456")
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EXPIRED_VERIFICATION_CODE);
    }

    // 일치하지 않는 인증 코드 오류 검증
    @Test
    void confirmVerificationCodeRejectsInvalidCode() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn("654321");
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn(1L);

        assertThatThrownBy(() -> emailVerificationService.confirmVerificationCode(
                new EmailVerificationConfirmRequest(EMAIL, "123456")
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_VERIFICATION_CODE);
        verify(redisTemplate).expire(ATTEMPT_KEY, CODE_EXPIRATION);
    }

    // 인증 코드 최대 실패 횟수 초과 잠금 검증
    @Test
    void confirmVerificationCodeLocksEmailAfterMaxAttempts() {
        prepareValueOperations();
        when(valueOperations.get(CODE_KEY)).thenReturn("654321");
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn(5L);

        assertThatThrownBy(() -> emailVerificationService.confirmVerificationCode(
                new EmailVerificationConfirmRequest(EMAIL, "123456")
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_VERIFICATION_LOCKED);

        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(ATTEMPT_KEY);
        verify(valueOperations).set(LOCK_KEY, "true", Duration.ofMinutes(5));
    }

    // 이메일 인증 완료 여부 검증
    @Test
    void validateVerifiedEmailRejectsUnverifiedEmail() {
        when(redisTemplate.hasKey(VERIFIED_KEY)).thenReturn(false);

        assertThatThrownBy(() -> emailVerificationService.validateVerifiedEmail(EMAIL))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_NOT_VERIFIED);
    }

    // Redis 문자열 연산 Mock 구성
    private void prepareValueOperations() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // 인증 코드 전송 Redis Mock 구성
    private void prepareSendOperations() {
        prepareValueOperations();
        when(valueOperations.setIfAbsent(
                COOLDOWN_KEY,
                "true",
                Duration.ofMinutes(1)
        )).thenReturn(true);
    }
}
