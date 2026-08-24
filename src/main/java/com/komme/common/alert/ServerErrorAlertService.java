package com.komme.common.alert;

import com.komme.common.alert.dto.DiscordEmbed;
import com.komme.common.alert.dto.DiscordMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerErrorAlertService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int ERROR_COLOR = 0xED4245;
    private static final int MAX_STACK_TRACE_LENGTH = 1000;
    private static final int MAX_EMBED_DESCRIPTION_LENGTH = 4000;

    private final DiscordAlertClient discordAlertClient;
    private final Environment environment;

    // 서버 에러 Discord 알림 전송 기능
    @Async("discordAlertExecutor")
    public void notify(int status, Exception exception, RequestContext requestContext) {
        discordAlertClient.send(buildMessage(status, exception, requestContext));
    }

    // Discord 알림 메시지 생성
    DiscordMessage buildMessage(int status, Exception exception, RequestContext requestContext) {
        String description = """
                ### 에러 발생 시간
                %s
                ### 실행 프로필
                %s
                ### 요청 엔드포인트
                %s
                ### 응답 상태
                %d
                ### 요청 클라이언트
                %s
                ### 에러 메시지
                %s
                ### 에러 스택 트레이스
                ```text
                %s
                ```
                """.formatted(
                ZonedDateTime.now(KOREA_ZONE).format(TIME_FORMATTER),
                activeProfiles(),
                endpoint(requestContext),
                status,
                client(requestContext),
                sanitize(exception.getMessage()),
                stackTrace(exception)
        );

        return new DiscordMessage(
                "# 서버 에러 발생",
                List.of(new DiscordEmbed(
                        "에러 정보",
                        truncate(description, MAX_EMBED_DESCRIPTION_LENGTH),
                        ERROR_COLOR
                ))
        );
    }

    // 실행 프로필 이름 생성
    private String activeProfiles() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            profiles = environment.getDefaultProfiles();
        }
        return String.join(",", Arrays.asList(profiles));
    }

    // 요청 엔드포인트 문자열 생성
    private String endpoint(RequestContext requestContext) {
        return "[%s] %s".formatted(requestContext.method(), sanitize(requestContext.url()));
    }

    // 요청 클라이언트 문자열 생성
    private String client(RequestContext requestContext) {
        String userIdentifier = !StringUtils.hasText(requestContext.userId())
                ? ""
                : " / [UserId]: " + sanitize(requestContext.userId());
        return "[IP]: %s%s / [User-Agent]: %s".formatted(
                sanitize(requestContext.ip()),
                userIdentifier,
                sanitize(requestContext.userAgent())
        );
    }

    // 예외 스택 트레이스 생성
    private String stackTrace(Exception exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return truncate(maskSensitiveValues(writer.toString()), MAX_STACK_TRACE_LENGTH);
    }

    // 알림 문자열 정제 기능
    private String sanitize(String value) {
        if (!StringUtils.hasText(value)) {
            return "-";
        }
        return maskSensitiveValues(value)
                .replace("\n", " ")
                .replace("\r", " ");
    }

    // 민감값 마스킹 기능
    private String maskSensitiveValues(String value) {
        return value
                .replaceAll("(?i)(bearer)\\s+\\S+", "$1 ***")
                .replaceAll(
                        "(?i)([\"']?[a-z0-9_-]*(authorization|cookie|token|secret|password)[a-z0-9_-]*[\"']?\\s*[:=]\\s*[\"']?)[^\\s&\"',;}]+",
                        "$1***"
                );
    }

    // 최대 길이 제한 기능
    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
