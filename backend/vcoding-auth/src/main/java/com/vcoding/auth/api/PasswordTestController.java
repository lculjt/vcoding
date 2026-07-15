package com.vcoding.auth.api;

import com.vcoding.auth.api.dto.PasswordEncryptForTestRequest;
import com.vcoding.auth.api.dto.PasswordEncryptForTestResponse;
import com.vcoding.auth.api.dto.PasswordPublicKeyResponse;
import com.vcoding.auth.application.crypto.PasswordCryptoService;
import com.vcoding.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("!prod")
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
@Tag(name = "密码测试工具", description = "仅非生产环境可用的密码传输加密辅助接口")
public class PasswordTestController {
    private final PasswordCryptoService passwordCryptoService;

    /**
     * Postman 测试辅助接口。生产环境禁用，避免对外提供明文密码换密文能力。
     */
    @Operation(summary = "测试环境密码加密", description = "把明文密码转换成登录、注册接口需要的 RSA-OAEP 密文。仅非 prod profile 可用。")
    @PostMapping("/encrypt-for-test")
    public ApiResponse<PasswordEncryptForTestResponse> encryptForTest(
            @Valid @RequestBody PasswordEncryptForTestRequest request
    ) {
        PasswordPublicKeyResponse publicKey = passwordCryptoService.currentPublicKey();
        return ApiResponse.success(new PasswordEncryptForTestResponse(
                passwordCryptoService.encryptForTest(request.getPassword()),
                publicKey.keyId(),
                publicKey.algorithm(),
                publicKey.expiresInSeconds()
        ));
    }
}
