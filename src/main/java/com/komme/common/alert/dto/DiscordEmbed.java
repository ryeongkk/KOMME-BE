package com.komme.common.alert.dto;

public record DiscordEmbed(
        String title,
        String description,
        Integer color
) {
}
