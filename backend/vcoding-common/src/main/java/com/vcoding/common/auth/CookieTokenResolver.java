package com.vcoding.common.auth;

import com.vcoding.common.auth.config.VcodingAuthProperties;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CookieTokenResolver {
    private final VcodingAuthProperties authProperties;

    /**
     * 从 Servlet 请求 Cookie 中读取统一登录态 Token。
     */
    public String resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new BusinessException(ErrorCode.AUTH_NOT_LOGIN);
        }

        for (Cookie cookie : cookies) {
            if (authProperties.getCookieName().equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        throw new BusinessException(ErrorCode.AUTH_NOT_LOGIN);
    }
}
