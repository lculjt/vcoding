package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "发送短信验证码请求")
public class SendSmsCodeRequest {
    @Schema(description = "短信场景：register、login、reset-password、bind-phone、change-phone", example = "register")
    @NotBlank(message = "短信场景不能为空")
    private String scene;

    @Schema(description = "中国大陆手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;

    @Schema(description = "图形验证码 ID", example = "f3b0f6a5c5d04ff3a14e8a9d1c6b7e01")
    @NotBlank(message = "图形验证码 ID 不能为空")
    private String captchaId;

    @Schema(description = "用户输入的图形验证码", example = "ABCD")
    @NotBlank(message = "图形验证码不能为空")
    private String captchaCode;
}
