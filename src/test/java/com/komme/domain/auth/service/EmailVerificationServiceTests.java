package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTests {

    private static final String EMAIL = "user@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationStore emailVerificationStore;

    @Mock
    private EmailVerificationCodeGenerator codeGenerator;

    @Mock
    private VerificationMailSender verificationMailSender;

    private EmailVerificationService emailVerificationService;

    // 이메일 인증 서비스 테스트 환경 구성
    @BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(
                userRepository,
                emailVerificationStore,
                codeGenerator,
                verificationMailSender
        );
    }

    // 인증 코드 전송 흐름과 이메일 정규화 검증
    @Test
    void sendVerificationCodeCoordinatesDependencies() {
        when(codeGenerator.generate()).thenReturn("123456");

        emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest(" USER@example.com ")
        );

        verify(userRepository).existsByEmail(EMAIL);
        verify(emailVerificationStore).prepareSend(EMAIL);
        verify(emailVerificationStore).saveCode(EMAIL, "123456");
        verify(verificationMailSender).send(EMAIL, "123456");
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

        verify(emailVerificationStore, never()).prepareSend(EMAIL);
    }

    // 이메일 발송 실패 시 저장 데이터 복구 검증
    @Test
    void sendVerificationCodeRollsBackStoreWhenMailFails() {
        when(codeGenerator.generate()).thenReturn("123456");
        doThrow(new MailSendException("mail failed"))
                .when(verificationMailSender)
                .send(EMAIL, "123456");

        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest(EMAIL)
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_SEND_FAILED);

        verify(emailVerificationStore).rollbackSend(EMAIL);
    }

    // 인증 코드 확인 Store 위임 검증
    @Test
    void confirmVerificationCodeDelegatesToStore() {
        emailVerificationService.confirmVerificationCode(
                new EmailVerificationConfirmRequest(" USER@example.com ", "123456")
        );

        verify(emailVerificationStore).confirmCode(EMAIL, "123456");
    }

    // 인증 상태 조회와 삭제 Store 위임 검증
    @Test
    void verificationStateOperationsDelegateToStore() {
        emailVerificationService.validateVerifiedEmail(EMAIL);
        emailVerificationService.deleteVerifiedEmail(EMAIL);

        verify(emailVerificationStore).validateVerified(EMAIL);
        verify(emailVerificationStore).deleteVerified(EMAIL);
    }
}
