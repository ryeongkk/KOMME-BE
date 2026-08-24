package com.komme.common.config;

import com.komme.domain.auth.jwt.JwtAuthenticationEntryPoint;
import com.komme.domain.auth.jwt.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] AUTH_PUBLIC_ENDPOINTS = {
            "/api/v1/auth/email-verifications/**",
            "/api/v1/auth/password-resets/**",
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/oauth/apple",
            "/api/v1/auth/oauth/google",
            "/api/v1/auth/tokens/reissue"
    };

    private static final String[] USER_PUBLIC_ENDPOINTS = {
            "/api/v1/users/nicknames/availability"
    };

    private static final String[] DOCS_PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] HEALTH_PUBLIC_ENDPOINTS = {
            "/health"
    };

    private static final String[] PUBLIC_ENDPOINTS = Stream.of(
                    AUTH_PUBLIC_ENDPOINTS,
                    USER_PUBLIC_ENDPOINTS,
                    DOCS_PUBLIC_ENDPOINTS,
                    HEALTH_PUBLIC_ENDPOINTS
            )
            .flatMap(Arrays::stream)
            .toArray(String[]::new);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // BCrypt 비밀번호 인코더 생성
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Stateless 기반 기본 보안 필터 체인 생성
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
