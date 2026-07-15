package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "测试环境密码加密响应")
public record PasswordEncryptForTestResponse(
        @Schema(description = "Base64 编码的 RSA-OAEP 密码密文")
        String passwordCiphertext,
        @Schema(description = "密码加密公钥 ID，登录或注册时需要原样传入", example = "202606260001")
        String passwordKeyId,
        @Schema(description = "前端加密算法", example = "RSA-OAEP-256")
        String algorithm,
        @Schema(description = "公钥剩余有效期，单位秒", example = "86400")
        long expiresInSeconds
) {
}
