package com.komme.domain.auth.service.email;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.PasswordResetConfirmRequest;
import com.komme.domain.auth.dto.request.PasswordResetSendRequest;
import com.komme.domain.auth.dto.response.PasswordResetTokenResponse;
import com.komme.domain.auth.enums.EmailVerificationPurpose;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.service.AuthUserReader;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.repository.UserRepository;

import java.util.Optional;

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
    private AuthUserReader authUserReader;

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
                authUserReader,
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
        verify(emailVerificationStore).prepareSend(EmailVerificationPurpose.SIGN_UP, EMAIL);
        verify(emailVerificationStore).saveCode(EmailVerificationPurpose.SIGN_UP, EMAIL, "123456");
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

        verify(emailVerificationStore, never()).prepareSend(EmailVerificationPurpose.SIGN_UP, EMAIL);
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

        verify(emailVerificationStore).rollbackSend(EmailVerificationPurpose.SIGN_UP, EMAIL);
    }

    // 비밀번호 재설정 인증 코드 전송 흐름 검증
    @Test
    void sendPasswordResetVerificationCodeCoordinatesDependencies() {
        User user = createLocalUser();
        when(authUserReader.findLocalByEmailForPasswordReset(EMAIL))
                .thenReturn(Optional.of(user));
        when(codeGenerator.generate()).thenReturn("123456");

        emailVerificationService.sendPasswordResetVerificationCode(
                new PasswordResetSendRequest(" USER@example.com ")
        );

        verify(emailVerificationStore).prepareSend(
                EmailVerificationPurpose.PASSWORD_RESET,
                EMAIL
        );
        verify(emailVerificationStore).saveCode(
                EmailVerificationPurpose.PASSWORD_RESET,
                EMAIL,
                "123456"
        );
        verify(verificationMailSender).send(EMAIL, "123456");
    }

    // 미가입 이메일 비밀번호 재설정 인증 코드 전송 성공 응답 검증
    @Test
    void sendPasswordResetVerificationCodeIgnoresUnregisteredEmail() {
        when(authUserReader.findLocalByEmailForPasswordReset(EMAIL))
                .thenReturn(Optional.empty());

        emailVerificationService.sendPasswordResetVerificationCode(
                new PasswordResetSendRequest(EMAIL)
        );

        verify(emailVerificationStore).prepareSend(
                EmailVerificationPurpose.PASSWORD_RESET,
                EMAIL
        );
        verify(emailVerificationStore, never()).saveCode(
                EmailVerificationPurpose.PASSWORD_RESET,
                EMAIL,
                "123456"
        );
        verify(verificationMailSender, never()).send(EMAIL, "123456");
    }

    // OAuth 이메일 비밀번호 재설정 인증 코드 전송 성공 응답 검증
    @Test
    void sendPasswordResetVerificationCodeIgnoresOAuthEmail() {
        when(authUserReader.findLocalByEmailForPasswordReset(EMAIL))
                .thenReturn(Optional.empty());

        emailVerificationService.sendPasswordResetVerificationCode(
                new PasswordResetSendRequest(EMAIL)
        );

        verify(emailVerificationStore).prepareSend(
                EmailVerificationPurpose.PASSWORD_RESET,
                EMAIL
        );
        verify(emailVerificationStore, never()).saveCode(
                EmailVerificationPurpose.PASSWORD_RESET,
                EMAIL,
                "123456"
        );
        verify(verificationMailSender, never()).send(EMAIL, "123456");
    }

    // 인증 코드 확인 Store 위임 검증
    @Test
    void confirmVerificationCodeDelegatesToStore() {
        emailVerificationService.confirmVerificationCode(
                new EmailVerificationConfirmRequest(" USER@example.com ", "123456")
        );

        verify(emailVerificationStore).confirmCode(EMAIL, "123456");
    }

    // 비밀번호 재설정 인증 코드 확인과 토큰 저장 검증
    @Test
    void confirmPasswordResetVerificationCodeIssuesResetToken() {
        PasswordResetTokenResponse response =
                emailVerificationService.confirmPasswordResetVerificationCode(
                        new PasswordResetConfirmRequest(" USER@example.com ", "123456")
                );

        org.assertj.core.api.Assertions.assertThat(response.resetToken()).isNotBlank();
        verify(emailVerificationStore).confirmPasswordResetCode(EMAIL, "123456");
        verify(emailVerificationStore).savePasswordResetToken(response.resetToken(), EMAIL);
    }

    // 인증 상태 조회와 삭제 Store 위임 검증
    @Test
    void verificationStateOperationsDelegateToStore() {
        emailVerificationService.validateVerifiedEmail(EMAIL);
        emailVerificationService.deleteVerifiedEmail(EMAIL);

        verify(emailVerificationStore).validateVerified(EMAIL);
        verify(emailVerificationStore).deleteVerified(EMAIL);
    }

    // 비밀번호 재설정 토큰 소비 Store 위임 검증
    @Test
    void consumePasswordResetTokenDelegatesToStore() {
        emailVerificationService.consumePasswordResetToken("reset-token");

        verify(emailVerificationStore).consumePasswordResetToken("reset-token");
    }

    // LOCAL 사용자 생성
    private User createLocalUser() {
        return User.createLocal(
                EMAIL,
                "encoded-password",
                "nickname"
        );
    }
}
