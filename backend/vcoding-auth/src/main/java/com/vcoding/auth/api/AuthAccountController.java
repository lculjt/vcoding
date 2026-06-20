package com.vcoding.auth.api;

import com.vcoding.auth.api.dto.CurrentUserResponse;
import com.vcoding.auth.api.dto.LoginRequest;
import com.vcoding.auth.api.dto.LoginResponse;
import com.vcoding.auth.api.dto.RegisterRequest;
import com.vcoding.auth.api.dto.RegisterResponse;
import com.vcoding.auth.api.dto.SmsLoginRequest;
import com.vcoding.auth.application.session.AuthSessionService;
import com.vcoding.auth.application.user.UserRegisterService;
import com.vcoding.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "账号接口", description = "统一用户中心账号注册、登录和会话相关接口")
public class AuthAccountController {
    private final UserRegisterService userRegisterService;
    private final AuthSessionService authSessionService;

    /**
     * 用户自助注册入口。管理员创建账号后续单独做后台接口，避免和开放注册混在一起。
     */
    @Operation(summary = "用户注册", description = "使用手机号短信验证码完成自助注册，注册成功后返回用户基础信息。")
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userRegisterService.register(request));
    }

    /**
     * 唯一登录入口的账号密码登录接口，登录成功后写入 HttpOnly Cookie。
     */
    @Operation(summary = "账号密码登录", description = "使用用户名或手机号加密码登录，成功后通过 HttpOnly Cookie 写入登录态。")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return ApiResponse.success(authSessionService.login(request, response));
    }

    /**
     * 唯一登录入口的手机号验证码登录接口，验证码必须来自 login 场景。
     */
    @Operation(summary = "手机号验证码登录", description = "使用手机号和 login 场景短信验证码登录，成功后通过 HttpOnly Cookie 写入登录态。")
    @PostMapping("/login/sms")
    public ApiResponse<LoginResponse> loginBySms(
            @Valid @RequestBody SmsLoginRequest request,
            HttpServletResponse response
    ) {
        return ApiResponse.success(authSessionService.loginBySms(request, response));
    }

    /**
     * 当前用户接口。前端门户和业务系统通过该接口判断用户是否已登录。
     */
    @Operation(summary = "获取当前用户", description = "从登录 Cookie 解析当前用户，并返回用户基础信息。")
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(HttpServletRequest request) {
        return ApiResponse.success(authSessionService.currentUser(request));
    }

    /**
     * 退出登录接口，清理浏览器中的统一登录态 Cookie。
     */
    @Operation(summary = "退出登录", description = "清理统一登录态 Cookie。")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        authSessionService.logout(response);
        return ApiResponse.success();
    }
}
