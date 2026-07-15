package com.vcoding.auth.application.session;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vcoding.auth.api.dto.CurrentUserResponse;
import com.vcoding.auth.api.dto.LoginRequest;
import com.vcoding.auth.api.dto.LoginResponse;
import com.vcoding.auth.api.dto.SmsLoginRequest;
import com.vcoding.auth.application.captcha.SmsCodeService;
import com.vcoding.auth.application.crypto.PasswordCryptoService;
import com.vcoding.auth.application.user.PasswordHashService;
import com.vcoding.auth.domain.sms.SmsScene;
import com.vcoding.auth.domain.user.UserStatus;
import com.vcoding.auth.infrastructure.persistence.entity.UserEntity;
import com.vcoding.auth.infrastructure.persistence.mapper.UserMapper;
import com.vcoding.common.auth.CookieTokenResolver;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.auth.JwtService;
import com.vcoding.common.auth.config.VcodingAuthProperties;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthSessionService {
    private final UserMapper userMapper;
    private final PasswordHashService passwordHashService;
    private final JwtService jwtService;
    private final VcodingAuthProperties authProperties;
    private final CookieTokenResolver cookieTokenResolver;
    private final SmsCodeService smsCodeService;
    private final PasswordCryptoService passwordCryptoService;

    /**
     * 账号密码登录。account 支持用户名或手机号，成功后写入统一 HttpOnly Cookie。
     */
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        String account = request.getAccount().trim();
        UserEntity user = findByUsernameOrPhone(account);
        ensureEnabled(user);

        String rawPassword = passwordCryptoService.decrypt(request.getPasswordCiphertext(), request.getPasswordKeyId());
        if (!passwordHashService.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_USERNAME_OR_PASSWORD);
        }

        return issueLoginResponse(user, response);
    }

    /**
     * 手机号验证码登录。验证码只接受 login 场景，避免注册、找回密码验证码被串用。
     */
    public LoginResponse loginBySms(SmsLoginRequest request, HttpServletResponse response) {
        String phone = request.getPhone().trim();
        smsCodeService.verifyAndDelete(SmsScene.LOGIN, phone, request.getSmsCode());

        UserEntity user = findByPhone(phone);
        ensureEnabled(user);

        return issueLoginResponse(user, response);
    }

    /**
     * 统一签发登录态，保证账号密码登录和短信登录写入同一种 Cookie。
     */
    private LoginResponse issueLoginResponse(UserEntity user, HttpServletResponse response) {
        String token = jwtService.createToken(toJwtCurrentUser(user));
        response.addHeader(HttpHeaders.SET_COOKIE, buildLoginCookie(token).toString());

        return new LoginResponse(toCurrentUser(user), authProperties.getJwtTtlSeconds());
    }

    /**
     * 获取当前登录用户。后续门户和业务系统可以用该接口判断是否已登录。
     */
    public CurrentUserResponse currentUser(HttpServletRequest request) {
        String token = cookieTokenResolver.resolve(request);
        CurrentUser currentUser = jwtService.parseToken(token);
        UserEntity user = userMapper.selectById(currentUser.userId());
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_NOT_LOGIN);
        }
        ensureEnabled(user);
        return toCurrentUser(user);
    }

    /**
     * 清理登录态 Cookie。JWT 本身无状态，第一阶段退出登录只负责让浏览器删除 Cookie。
     */
    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildLogoutCookie().toString());
    }

    private UserEntity findByUsernameOrPhone(String account) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, account)
                .or()
                .eq(UserEntity::getPhone, account)
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_USERNAME_OR_PASSWORD);
        }
        return user;
    }

    private UserEntity findByPhone(String phone) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getPhone, phone)
                .last("LIMIT 1"));
        if (user == null) {
            // 登录阶段不暴露手机号是否已注册，避免被外部枚举账号。
            throw new BusinessException(ErrorCode.AUTH_INVALID_USERNAME_OR_PASSWORD);
        }
        return user;
    }

    private void ensureEnabled(UserEntity user) {
        if (UserStatus.fromCode(user.getStatus()) != UserStatus.ENABLED) {
            throw new BusinessException(ErrorCode.AUTH_USER_DISABLED);
        }
    }

    private CurrentUserResponse toCurrentUser(UserEntity user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getPhone(),
                Boolean.TRUE.equals(user.getAdminFlag())
        );
    }

    private CurrentUser toJwtCurrentUser(UserEntity user) {
        return new CurrentUser(
                user.getId(),
                user.getUsername(),
                user.getPhone(),
                Boolean.TRUE.equals(user.getAdminFlag())
        );
    }

    private ResponseCookie buildLoginCookie(String token) {
        return ResponseCookie.from(authProperties.getCookieName(), token)
                .httpOnly(true)
                .secure(authProperties.isCookieSecure())
                .sameSite(authProperties.getCookieSameSite())
                .path(authProperties.getCookiePath())
                .maxAge(Duration.ofSeconds(authProperties.getJwtTtlSeconds()))
                .build();
    }

    private ResponseCookie buildLogoutCookie() {
        return ResponseCookie.from(authProperties.getCookieName(), "")
                .httpOnly(true)
                .secure(authProperties.isCookieSecure())
                .sameSite(authProperties.getCookieSameSite())
                .path(authProperties.getCookiePath())
                .maxAge(Duration.ZERO)
                .build();
    }
}
