package com.komme.domain.auth.service;

import com.komme.domain.auth.properties.AuthMailProperties;
import com.komme.domain.auth.properties.EmailVerificationProperties;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VerificationMailSender {

    private final JavaMailSender mailSender;
    private final AuthMailProperties mailProperties;
    private final EmailVerificationProperties emailVerificationProperties;

    // Gmail SMTP 인증 코드 전송 기능
    public void send(String email, String verificationCode) {
        mailSender.send(createMessage(email, verificationCode));
    }

    // 이메일 인증 메시지 생성
    private SimpleMailMessage createMessage(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getSender());
        message.setTo(email);
        message.setSubject("[KOMME] 이메일 인증 코드");
        message.setText(
                "이메일 인증 코드는 " + verificationCode + "입니다. "
                        + emailVerificationProperties.getCodeExpiration().toMinutes()
                        + "분 안에 입력해 주세요."
        );
        return message;
    }
}
