package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
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

@Service
public class EmailVerificationService {

    private static final String VERIFICATION_CODE_KEY_PREFIX = "auth:email-verification:code:";
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom;
    private final AuthMailProperties mailProperties;
    private final EmailVerificationProperties emailVerificationProperties;

    // 이메일 인증 서비스 의존성과 설정값 주입
    public EmailVerificationService(
            UserRepository userRepository,
            StringRedisTemplate redisTemplate,
            JavaMailSender mailSender,
            AuthMailProperties mailProperties,
            EmailVerificationProperties emailVerificationProperties
    ) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
        this.secureRandom = new SecureRandom();
        this.mailProperties = mailProperties;
        this.emailVerificationProperties = emailVerificationProperties;
    }

    // 이메일 인증 코드 전송 및 Redis 저장 기능
    public void sendVerificationCode(EmailVerificationSendRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        validateEmailNotRegistered(email);

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
            throw new GeneralException(AuthErrorStatus.EMAIL_SEND_FAILED, exception);
        }
    }

    // 가입된 이메일 여부 확인 기능
    private void validateEmailNotRegistered(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
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
