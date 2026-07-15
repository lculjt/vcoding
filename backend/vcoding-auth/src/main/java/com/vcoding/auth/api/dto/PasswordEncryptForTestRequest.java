package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "测试环境密码加密请求")
public class PasswordEncryptForTestRequest {
    @Schema(description = "明文密码，仅用于本地或测试环境 Postman 调试", example = "Vcoding@123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须在 8 到 64 位之间")
    private String password;
}
