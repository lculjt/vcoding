package com.vcoding.auth.api;

import com.vcoding.auth.api.dto.ImageCaptchaResponse;
import com.vcoding.auth.api.dto.SendSmsCodeRequest;
import com.vcoding.auth.api.dto.SendSmsCodeResponse;
import com.vcoding.auth.application.captcha.ImageCaptchaService;
import com.vcoding.auth.application.captcha.SmsCodeService;
import com.vcoding.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "验证码接口", description = "图形验证码和短信验证码相关接口")
public class CaptchaController {
    private final ImageCaptchaService imageCaptchaService;
    private final SmsCodeService smsCodeService;

    /**
     * 创建图形验证码。前端拿到 captchaId 和图片后，后续发送短信验证码时必须一并提交。
     */
    @Operation(summary = "获取图形验证码", description = "返回 captchaId、SVG data URL 和有效期。发送短信验证码前需要先完成图形验证码校验。")
    @GetMapping("/captcha/image")
    public ApiResponse<ImageCaptchaResponse> createImageCaptcha() {
        return ApiResponse.success(imageCaptchaService.createImageCaptcha());
    }

    /**
     * 发送短信验证码。这里先解析客户端 IP，后续由应用服务统一处理图形验证码和限流规则。
     */
    @Operation(summary = "发送短信验证码", description = "支持注册、登录、找回密码、绑定手机号和换绑手机号等短信场景。")
    @PostMapping("/sms/send")
    public ApiResponse<SendSmsCodeResponse> sendSmsCode(
            @Valid @RequestBody SendSmsCodeRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.success(smsCodeService.sendCode(request, clientIp(servletRequest)));
    }

    /**
     * 解析当前 HTTP 请求对应的客户端 IP，供短信发送接口的 IP 维度限流使用。
     * <p>
     * 请求链路通常是「浏览器 → 网关/Nginx → vcoding-auth」。经过反向代理后，
     * {@link HttpServletRequest#getRemoteAddr()} 往往只能拿到代理机器的内网地址，
     * 无法用于识别真实用户，因此需要优先读取代理写入的转发头。
     * <p>
     * 解析优先级：
     * <ol>
     *   <li>{@code X-Forwarded-For}：最常见的代理转发头，可能包含多个 IP</li>
     *   <li>{@code X-Real-IP}：Nginx 等反向代理常用的单 IP 头</li>
     *   <li>{@code remoteAddr}：无代理或本地直连时的兜底值，开发环境常见 {@code 127.0.0.1}</li>
     * </ol>
     * <p>
     * 安全前提：仅当前置网关/反向代理会覆写或追加转发头、且后端不直接暴露公网时，
     * 才应信任这些请求头；否则客户端可能伪造 IP 绕过限流。
     *
     * @param request 当前 HTTP 请求
     * @return 用于限流的客户端 IP 字符串，不会返回 null
     */
    private String clientIp(HttpServletRequest request) {
        // 标准代理链格式：clientIp, proxy1, proxy2 ...
        // 取第一个值即最原始的客户端地址。
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        // 部分 Nginx 配置只写入 X-Real-IP，作为第二优先级兜底。
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }

        // 本地开发或未配置转发头时，直接使用 TCP 连接对端地址。
        return request.getRemoteAddr();
    }
}
