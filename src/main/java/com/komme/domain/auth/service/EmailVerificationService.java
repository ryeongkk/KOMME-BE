package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.util.EmailNormalizer;
import com.komme.domain.user.service.UserReader;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserReader userReader;
    private final EmailVerificationStore emailVerificationStore;
    private final EmailVerificationCodeGenerator codeGenerator;
    private final VerificationMailSender verificationMailSender;

    // 이메일 인증 코드 전송 기능
    public void sendVerificationCode(EmailVerificationSendRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        validateEmailNotRegistered(email);
        emailVerificationStore.prepareSend(email);

        String verificationCode = codeGenerator.generate();
        emailVerificationStore.saveCode(email, verificationCode);
        sendVerificationEmail(email, verificationCode);
    }

    // 이메일 인증 코드 확인 기능
    public void confirmVerificationCode(EmailVerificationConfirmRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        emailVerificationStore.confirmCode(email, request.verificationCode());
    }

    // 이메일 인증 완료 여부 검증 기능
    public void validateVerifiedEmail(String email) {
        emailVerificationStore.validateVerified(email);
    }

    // 이메일 인증 완료 플래그 삭제 기능
    public void deleteVerifiedEmail(String email) {
        emailVerificationStore.deleteVerified(email);
    }

    // 가입된 이메일 여부 확인 기능
    private void validateEmailNotRegistered(String email) {
        if (userReader.existsByEmail(email)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
        }
    }

    // 인증 이메일 전송 및 실패 데이터 정리 기능
    private void sendVerificationEmail(String email, String verificationCode) {
        try {
            verificationMailSender.send(email, verificationCode);
        } catch (MailException exception) {
            emailVerificationStore.rollbackSend(email);
            throw new GeneralException(AuthErrorStatus.EMAIL_SEND_FAILED, exception);
        }
    }
}
