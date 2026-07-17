package com.komme.domain.auth.service;

import com.komme.domain.auth.properties.AuthMailProperties;
import com.komme.domain.auth.properties.EmailVerificationProperties;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerificationMailSenderTests {

    @Mock
    private JavaMailSender mailSender;

    private VerificationMailSender verificationMailSender;

    // 인증 메일 전송 테스트 환경 구성
    @BeforeEach
    void setUp() {
        verificationMailSender = new VerificationMailSender(
                mailSender,
                new AuthMailProperties("sender@example.com"),
                new EmailVerificationProperties(
                        Duration.ofMinutes(5),
                        Duration.ofMinutes(30),
                        5,
                        Duration.ofMinutes(1),
                        Duration.ofMinutes(5)
                )
        );
    }

    // 인증 메일 발신자와 수신자 및 본문 생성 검증
    @Test
    void sendCreatesVerificationMail() {
        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verificationMailSender.send("user@example.com", "123456");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getFrom()).isEqualTo("sender@example.com");
        assertThat(message.getTo()).containsExactly("user@example.com");
        assertThat(message.getSubject()).isEqualTo("[KOMME] 이메일 인증 코드");
        assertThat(message.getText()).contains("123456", "5분");
    }
}
