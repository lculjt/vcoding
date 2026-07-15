package com.vcoding.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcoding.common.auth.AuthHeaderNames;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.auth.GatewayUserHeaderService;
import com.vcoding.common.auth.JwtService;
import com.vcoding.common.auth.config.VcodingAuthProperties;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ApiResponse;
import com.vcoding.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GatewayAuthenticationFilter implements GlobalFilter, Ordered {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/captcha/image",
            "/api/auth/sms/send",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/login/sms",
            "/api/auth/logout",
            "/api/auth/health",
            "/api/auth/password/public-key",
            "/api/auth/password/encrypt-for-test"
    );

    private final VcodingAuthProperties authProperties;
    private final JwtService jwtService;
    private final GatewayUserHeaderService gatewayUserHeaderService;
    private final ObjectMapper objectMapper;

    /**
     * Gateway 第一层鉴权：公开接口放行，其余接口必须携带有效登录 Cookie。
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (shouldSkipAuth(request)) {
            return chain.filter(stripInternalHeaders(exchange));
        }

        try {
            String token = resolveToken(exchange);
            CurrentUser currentUser = jwtService.parseToken(token);
            return chain.filter(withCurrentUserHeaders(exchange, currentUser));
        } catch (BusinessException exception) {
            return writeError(exchange.getResponse(), exception.getErrorCode(), exception.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean shouldSkipAuth(ServerHttpRequest request) {
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return true;
        }
        return PUBLIC_PATHS.contains(request.getURI().getPath());
    }

    private String resolveToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(authProperties.getCookieName());
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            throw new BusinessException(ErrorCode.AUTH_NOT_LOGIN);
        }
        return cookie.getValue();
    }

    private ServerWebExchange withCurrentUserHeaders(ServerWebExchange exchange, CurrentUser currentUser) {
        Map<String, String> signedHeaders = gatewayUserHeaderService.buildSignedHeaders(currentUser);
        return exchange.mutate()
                .request(builder -> {
                    removeInternalHeaders(builder);
                    signedHeaders.forEach(builder::header);
                })
                .build();
    }

    private ServerWebExchange stripInternalHeaders(ServerWebExchange exchange) {
        return exchange.mutate()
                .request(this::removeInternalHeaders)
                .build();
    }

    private void removeInternalHeaders(ServerHttpRequest.Builder builder) {
        builder.headers(headers -> {
            headers.remove(AuthHeaderNames.GATEWAY_SIGNATURE);
            headers.remove(AuthHeaderNames.GATEWAY_TIMESTAMP);
            headers.remove(AuthHeaderNames.USER_ID);
            headers.remove(AuthHeaderNames.USERNAME);
            headers.remove(AuthHeaderNames.PHONE);
            headers.remove(AuthHeaderNames.ADMIN);
        });
    }

    private Mono<Void> writeError(ServerHttpResponse response, ErrorCode errorCode, String message) {
        response.setStatusCode(HttpStatus.valueOf(errorCode.getHttpStatus()));
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        byte[] body = toJsonBytes(ApiResponse.fail(errorCode, message));
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    private byte[] toJsonBytes(ApiResponse<Object> response) {
        try {
            return objectMapper.writeValueAsString(response).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException exception) {
            return "{\"code\":\"COMMON_INTERNAL_ERROR\",\"message\":\"系统异常\"}"
                    .getBytes(StandardCharsets.UTF_8);
        }
    }
}
