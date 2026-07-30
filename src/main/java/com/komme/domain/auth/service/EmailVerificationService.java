package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.PasswordResetConfirmRequest;
import com.komme.domain.auth.dto.request.PasswordResetSendRequest;
import com.komme.domain.auth.dto.response.PasswordResetTokenResponse;
import com.komme.domain.auth.enums.EmailVerificationPurpose;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.util.EmailNormalizer;
import com.komme.domain.user.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int PASSWORD_RESET_TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AuthUserReader authUserReader;
    private final EmailVerificationStore emailVerificationStore;
    private final EmailVerificationCodeGenerator codeGenerator;
    private final VerificationMailSender verificationMailSender;

    // 이메일 인증 코드 전송 기능
    public void sendVerificationCode(EmailVerificationSendRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        validateEmailNotRegistered(email);
        sendVerificationCode(EmailVerificationPurpose.SIGN_UP, email);
    }

    // 비밀번호 재설정 인증 코드 전송 기능
    public void sendPasswordResetVerificationCode(PasswordResetSendRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        if (authUserReader.findLocalByEmailForPasswordReset(email).isEmpty()) {
            return;
        }

        sendVerificationCode(EmailVerificationPurpose.PASSWORD_RESET, email);
    }

    // 이메일 인증 코드 확인 기능
    public void confirmVerificationCode(EmailVerificationConfirmRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        emailVerificationStore.confirmCode(email, request.verificationCode());
    }

    // 비밀번호 재설정 인증 코드 확인 및 토큰 발급 기능
    public PasswordResetTokenResponse confirmPasswordResetVerificationCode(
            PasswordResetConfirmRequest request
    ) {
        String email = EmailNormalizer.normalize(request.email());
        emailVerificationStore.confirmPasswordResetCode(email, request.verificationCode());

        String resetToken = generatePasswordResetToken();
        emailVerificationStore.savePasswordResetToken(resetToken, email);
        return PasswordResetTokenResponse.of(resetToken);
    }

    // 이메일 인증 완료 여부 검증 기능
    public void validateVerifiedEmail(String email) {
        emailVerificationStore.validateVerified(email);
    }

    // 이메일 인증 완료 플래그 삭제 기능
    public void deleteVerifiedEmail(String email) {
        emailVerificationStore.deleteVerified(email);
    }

    // 비밀번호 재설정 토큰 소비 기능
    public String consumePasswordResetToken(String resetToken) {
        return emailVerificationStore.consumePasswordResetToken(resetToken);
    }

    // 가입된 이메일 여부 확인 기능
    private void validateEmailNotRegistered(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
        }
    }

    // 목적별 인증 코드 전송 기능
    private void sendVerificationCode(EmailVerificationPurpose purpose, String email) {
        emailVerificationStore.prepareSend(purpose, email);

        String verificationCode = codeGenerator.generate();
        emailVerificationStore.saveCode(purpose, email, verificationCode);
        sendVerificationEmail(purpose, email, verificationCode);
    }

    // 인증 이메일 전송 및 실패 데이터 정리 기능
    private void sendVerificationEmail(
            EmailVerificationPurpose purpose,
            String email,
            String verificationCode
    ) {
        try {
            verificationMailSender.send(email, verificationCode);
        } catch (MailException exception) {
            emailVerificationStore.rollbackSend(purpose, email);
            throw new GeneralException(AuthErrorStatus.EMAIL_SEND_FAILED, exception);
        }
    }

    // 비밀번호 재설정 토큰 생성
    private String generatePasswordResetToken() {
        byte[] bytes = new byte[PASSWORD_RESET_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
