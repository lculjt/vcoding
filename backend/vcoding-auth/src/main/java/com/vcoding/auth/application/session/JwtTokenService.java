package com.vcoding.auth.application.session;

import com.vcoding.auth.config.AuthProperties;
import com.vcoding.auth.infrastructure.persistence.entity.UserEntity;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final AuthProperties authProperties;

    /**
     * 为登录成功的用户签发 JWT。Token 放入 HttpOnly Cookie，前端不直接读取明文。
     */
    public String createToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(authProperties.getJwtTtlSeconds());

        return Jwts.builder()
                .issuer(authProperties.getJwtIssuer())
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("phone", user.getPhone())
                .claim("adminFlag", Boolean.TRUE.equals(user.getAdminFlag()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey())
                .compact();
    }

    /**
     * 解析并校验 JWT，任何过期、篡改或格式错误都会转换为统一业务错误。
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
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
    }

    private SecretKey secretKey() {
        byte[] secretBytes = authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
