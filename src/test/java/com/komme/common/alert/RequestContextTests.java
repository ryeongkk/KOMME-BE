package com.komme.common.alert;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestContextTests {

    // 프록시 요청 헤더 기반 요청 정보 생성 검증
    @Test
    void fromUsesForwardedIpAndPrincipal() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Principal principal = mock(Principal.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://api.example.com/api/v1/auth/login"));
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10, 10.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("1");

        RequestContext requestContext = RequestContext.from(request);

        assertThat(requestContext.method()).isEqualTo("POST");
        assertThat(requestContext.url()).isEqualTo("https://api.example.com/api/v1/auth/login");
        assertThat(requestContext.ip()).isEqualTo("203.0.113.10");
        assertThat(requestContext.userId()).isEqualTo("1");
        assertThat(requestContext.userAgent()).isEqualTo("JUnit");
    }

    // 프록시 요청 헤더가 없을 때 원격 주소 사용 검증
    @Test
    void fromFallsBackToRemoteAddress() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://api.example.com/health"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        RequestContext requestContext = RequestContext.from(request);

        assertThat(requestContext.ip()).isEqualTo("127.0.0.1");
        assertThat(requestContext.userId()).isNull();
    }
}
