package com.komme.common.config;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    // Discord 알림 전용 비동기 실행기 생성
    @Bean(name = "discordAlertExecutor")
    public Executor discordAlertExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("discord-alert-");
        executor.setRejectedExecutionHandler(discardWithWarningPolicy());
        return executor;
    }

    // Discord 알림 큐 초과 로그 정책 생성
    private RejectedExecutionHandler discardWithWarningPolicy() {
        return (runnable, executor) -> log.warn("[*] Discord alert dropped: queue full");
    }
}
