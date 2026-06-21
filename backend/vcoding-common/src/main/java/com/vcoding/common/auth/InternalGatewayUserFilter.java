package com.vcoding.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ApiResponse;
import com.vcoding.common.response.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class InternalGatewayUserFilter extends OncePerRequestFilter {
    private final GatewayUserHeaderService gatewayUserHeaderService;
    private final ObjectMapper objectMapper;

    /**
     * 内部服务的第二层校验：只信任 Gateway 签名后的用户头，并写入当前请求上下文。
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            CurrentUser currentUser = gatewayUserHeaderService.verifyAndParse(readGatewayHeaders(request));
            AuthContext.set(currentUser);
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            writeError(response, exception.getErrorCode(), exception.getMessage());
        } finally {
            AuthContext.clear();
        }
    }

    private Map<String, String> readGatewayHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put(AuthHeaderNames.USER_ID, request.getHeader(AuthHeaderNames.USER_ID));
        headers.put(AuthHeaderNames.USERNAME, request.getHeader(AuthHeaderNames.USERNAME));
        headers.put(AuthHeaderNames.PHONE, request.getHeader(AuthHeaderNames.PHONE));
        headers.put(AuthHeaderNames.ADMIN, request.getHeader(AuthHeaderNames.ADMIN));
        headers.put(AuthHeaderNames.GATEWAY_TIMESTAMP, request.getHeader(AuthHeaderNames.GATEWAY_TIMESTAMP));
        headers.put(AuthHeaderNames.GATEWAY_SIGNATURE, request.getHeader(AuthHeaderNames.GATEWAY_SIGNATURE));
        return headers;
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(errorCode.getHttpStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(errorCode, message)));
    }
}
