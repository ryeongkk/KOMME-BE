package com.komme.common.alert;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

public record RequestContext(
        String method,
        String url,
        String ip,
        String userId,
        String userAgent
) {

    // HTTP 요청 정보 복사본 생성
    public static RequestContext from(HttpServletRequest request) {
        return new RequestContext(
                request.getMethod(),
                request.getRequestURL().toString(),
                clientIp(request),
                userId(request),
                request.getHeader("User-Agent")
        );
    }

    // 클라이언트 IP 추출 기능
    private static String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // 인증 사용자 식별자 추출 기능
    private static String userId(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
            return null;
        }
        return request.getUserPrincipal().getName();
    }
}
