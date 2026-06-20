package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "账号密码登录请求")
public class LoginRequest {
    @Schema(description = "用户名或手机号", example = "zhangsan")
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(description = "登录密码", example = "Vcoding@123")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须在 8 到 64 位之间")
    private String password;
}
