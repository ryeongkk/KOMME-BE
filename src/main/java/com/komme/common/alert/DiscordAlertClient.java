package com.komme.common.alert;

import com.komme.common.alert.dto.DiscordMessage;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DiscordAlertClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);

    private final DiscordAlertProperties properties;
    private final WebClient webClient;

    // Discord 알림 클라이언트 생성
    public DiscordAlertClient(
            DiscordAlertProperties properties,
            @Qualifier("discordAlertWebClient") WebClient webClient
    ) {
        this.properties = properties;
        this.webClient = webClient;
    }

    // Discord Webhook 메시지 전송 기능
    public void send(DiscordMessage message) {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getWebhookUrl())) {
            return;
        }

        try {
            webClient.post()
                    .uri(properties.getWebhookUrl())
                    .bodyValue(message)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(REQUEST_TIMEOUT)
                    .block();
        } catch (Exception exception) {
            log.warn("[*] Discord alert send failed: {}", exception.getClass().getSimpleName());
        }
    }
}
