package com.komme.domain.auth.jwt;

import com.komme.common.base.status.ErrorStatus;
import com.komme.domain.auth.exception.AuthErrorStatus;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationEntryPointTests {

    private final JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();

    // 요청 인증 오류 상태 응답 검증
    @Test
    void commenceUsesRequestAuthErrorStatus() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setAttribute(
                JwtAuthenticationFilter.AUTH_ERROR_STATUS_ATTRIBUTE,
                AuthErrorStatus.EXPIRED_TOKEN
        );

        entryPoint.commence(request, response, null);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains(AuthErrorStatus.EXPIRED_TOKEN.getCode());
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    }

    // 기본 인증 오류 상태 응답 검증
    @Test
    void commenceUsesDefaultUnauthorizedWithoutRequestStatus() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response, null);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains(ErrorStatus.UNAUTHORIZED.getCode());
    }
}
