package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "发送短信验证码响应")
public class SendSmsCodeResponse {
    @Schema(description = "短信场景", example = "register")
    private final String scene;

    @Schema(description = "手机号", example = "13800138000")
    private final String phone;

    @Schema(description = "短信验证码有效期，单位秒", example = "300")
    private final long expiresInSeconds;

    @Schema(description = "再次发送冷却时间，单位秒", example = "60")
    private final long cooldownSeconds;
}
