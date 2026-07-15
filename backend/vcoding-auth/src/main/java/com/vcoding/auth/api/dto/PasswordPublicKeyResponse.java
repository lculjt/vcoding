package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "密码传输加密公钥响应")
public record PasswordPublicKeyResponse(
        @Schema(description = "公钥 ID，提交加密密码时需要回传", example = "202606260001")
        String keyId,
        @Schema(description = "Base64 编码的 X.509 SubjectPublicKeyInfo 公钥", example = "MIIBIjANBg...")
        String publicKey,
        @Schema(description = "前端加密算法", example = "RSA-OAEP-256")
        String algorithm,
        @Schema(description = "公钥有效期，单位秒", example = "86400")
        long expiresInSeconds
) {
}
