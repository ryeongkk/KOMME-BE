package com.komme.domain.auth.jwt;

import com.komme.common.base.status.BaseStatus;
import com.komme.common.base.status.ErrorStatus;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 인증 실패 ApiResponse 반환 기능
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        BaseStatus errorStatus = resolveErrorStatus(request);
        response.setStatus(errorStatus.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(createErrorBody(errorStatus));
    }

    // 요청에 저장된 인증 오류 조회 기능
    private BaseStatus resolveErrorStatus(HttpServletRequest request) {
        Object errorStatus = request.getAttribute(
                JwtAuthenticationFilter.AUTH_ERROR_STATUS_ATTRIBUTE
        );

        if (errorStatus instanceof BaseStatus baseStatus) {
            return baseStatus;
        }

        return ErrorStatus.UNAUTHORIZED;
    }

    // 인증 실패 JSON 본문 생성 기능
    private String createErrorBody(BaseStatus errorStatus) {
        return """
                {
                  "isSuccess": false,
                  "code": "%s",
                  "message": "%s"
                }
                """.formatted(errorStatus.getCode(), errorStatus.getMessage());
    }
}
