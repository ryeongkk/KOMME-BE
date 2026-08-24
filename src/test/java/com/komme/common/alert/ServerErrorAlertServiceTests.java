package com.komme.common.alert;

import com.komme.common.alert.dto.DiscordMessage;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServerErrorAlertServiceTests {

    private final DiscordAlertClient discordAlertClient = mock(DiscordAlertClient.class);
    private final Environment environment = mock(Environment.class);
    private final ServerErrorAlertService serverErrorAlertService =
            new ServerErrorAlertService(discordAlertClient, environment);

    // 서버 오류 알림 메시지 구성 검증
    @Test
    void buildMessageContainsRequestAndErrorContext() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });
        RequestContext requestContext = new RequestContext(
                "POST",
                "https://api.example.com/api/v1/auth/login",
                "203.0.113.10",
                "1",
                "JUnit"
        );
        RuntimeException exception = new RuntimeException("database connection failed");

        DiscordMessage message = serverErrorAlertService.buildMessage(500, exception, requestContext);

        String description = message.embeds().getFirst().description();
        assertThat(message.content()).isEqualTo("# 서버 에러 발생");
        assertThat(description).contains("prod");
        assertThat(description).contains("[POST] https://api.example.com/api/v1/auth/login");
        assertThat(description).contains("500");
        assertThat(description).contains("[IP]: 203.0.113.10 / [UserId]: 1 / [User-Agent]: JUnit");
        assertThat(description).contains("database connection failed");
    }

    // 민감 정보 마스킹 검증
    @Test
    void buildMessageMasksSensitiveValues() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });
        RequestContext requestContext = new RequestContext(
                "GET",
                "https://api.example.com/error?token=secret-token&password=secret-password",
                "127.0.0.1",
                null,
                "Bearer access-token"
        );
        RuntimeException exception = new RuntimeException("Authorization=Bearer access-token password=secret");

        DiscordMessage message = serverErrorAlertService.buildMessage(500, exception, requestContext);

        String description = message.embeds().getFirst().description();
        assertThat(description).contains("token=***");
        assertThat(description).contains("password=***");
        assertThat(description).contains("Bearer ***");
        assertThat(description).doesNotContain("secret-token");
        assertThat(description).doesNotContain("secret-password");
        assertThat(description).doesNotContain("access-token");
    }

    // JSON 및 헤더 형태 민감 정보 마스킹 검증
    @Test
    void buildMessageMasksJsonAndHeaderStyleSensitiveValues() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });
        RequestContext requestContext = new RequestContext(
                "GET",
                "https://api.example.com/error",
                "127.0.0.1",
                null,
                "JUnit"
        );
        RuntimeException exception = new RuntimeException(
                "Cookie: sessionId=abc123; refreshToken=refresh-secret "
                        + "{\"error\":\"invalid_client\",\"client_secret\":\"client-secret-value\"}"
        );

        DiscordMessage message = serverErrorAlertService.buildMessage(500, exception, requestContext);

        String description = message.embeds().getFirst().description();
        assertThat(description).contains("Cookie: ***");
        assertThat(description).contains("refreshToken=***");
        assertThat(description).contains("\"client_secret\":\"***");
        assertThat(description).doesNotContain("sessionId=abc123");
        assertThat(description).doesNotContain("refresh-secret");
        assertThat(description).doesNotContain("client-secret-value");
    }

    // Discord 알림 클라이언트 위임 검증
    @Test
    void notifyDelegatesToDiscordAlertClient() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });
        RequestContext requestContext = new RequestContext(
                "GET",
                "https://api.example.com/health",
                "127.0.0.1",
                null,
                "JUnit"
        );
        RuntimeException exception = new RuntimeException("error");

        serverErrorAlertService.notify(500, exception, requestContext);

        verify(discordAlertClient).send(any(DiscordMessage.class));
    }
}
