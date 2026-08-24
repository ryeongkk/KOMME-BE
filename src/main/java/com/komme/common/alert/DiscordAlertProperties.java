package com.komme.common.alert;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "discord.alert")
public class DiscordAlertProperties {

    private final boolean enabled;
    private final String webhookUrl;

    // Discord 알림 설정값 생성
    public DiscordAlertProperties(boolean enabled, String webhookUrl) {
        this.enabled = enabled;
        this.webhookUrl = webhookUrl;
    }
}
