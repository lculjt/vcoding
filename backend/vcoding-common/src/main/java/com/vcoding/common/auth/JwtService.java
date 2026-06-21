package com.vcoding.common.auth;

import com.vcoding.common.auth.config.VcodingAuthProperties;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtService {
    private final VcodingAuthProperties authProperties;

    /**
     * 为登录成功的用户签发 JWT。Token 放入 HttpOnly Cookie，前端不直接读取明文。
     */
    public String createToken(CurrentUser currentUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(authProperties.getJwtTtlSeconds());

        return Jwts.builder()
                .issuer(authProperties.getJwtIssuer())
                .subject(String.valueOf(currentUser.userId()))
                .claim("username", currentUser.username())
                .claim("phone", currentUser.phone())
                .claim("adminFlag", currentUser.adminFlag())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey())
                .compact();
    }

    /**
     * 解析并校验 JWT，任何过期、篡改或格式错误都会转换为统一业务错误。
     */
    public CurrentUser parseToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey())
                    .requireIssuer(authProperties.getJwtIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        return new CurrentUser(
                parseUserId(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("phone", String.class),
                Boolean.TRUE.equals(claims.get("adminFlag", Boolean.class))
        );
    }

    private Long parseUserId(String subject) {
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    private SecretKey secretKey() {
        byte[] secretBytes = authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
