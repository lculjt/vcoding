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

    @Schema(description = "Base64 编码的 RSA-OAEP 密码密文", example = "m3n...")
    @NotBlank(message = "密码密文不能为空")
    @Size(max = 512, message = "密码密文长度不能超过 512 位")
    private String passwordCiphertext;

    @Schema(description = "密码加密公钥 ID", example = "202606260001")
    @NotBlank(message = "密码加密公钥 ID 不能为空")
    private String passwordKeyId;
}
