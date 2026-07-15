package com.vcoding.auth.application.crypto;

import com.vcoding.auth.api.dto.PasswordPublicKeyResponse;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class PasswordCryptoService {
    private static final String KEY_ALGORITHM = "RSA";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String FRONTEND_ALGORITHM = "RSA-OAEP-256";
    private static final int KEY_SIZE = 2048;
    private static final long KEY_TTL_SECONDS = 24 * 60 * 60;

    private final String keyId;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final Instant expiresAt;

    public PasswordCryptoService() {
        KeyPair keyPair = generateKeyPair();
        this.keyId = UUID.randomUUID().toString().replace("-", "");
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
        this.expiresAt = Instant.now().plusSeconds(KEY_TTL_SECONDS);
    }

    /**
     * 返回前端用于密码传输加密的公钥。私钥只保存在当前服务进程内，不落库。
     */
    public PasswordPublicKeyResponse currentPublicKey() {
        return new PasswordPublicKeyResponse(
                keyId,
                Base64.getEncoder().encodeToString(publicKey.getEncoded()),
                FRONTEND_ALGORITHM,
                Math.max(0, expiresAt.getEpochSecond() - Instant.now().getEpochSecond())
        );
    }

    /**
     * 解密前端通过 RSA-OAEP-SHA256 加密后的密码明文，再交给密码哈希服务处理。
     */
    public String decrypt(String passwordCiphertext, String passwordKeyId) {
        if (!keyId.equals(passwordKeyId) || Instant.now().isAfter(expiresAt)) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "密码加密公钥已失效，请刷新页面后重试");
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            OAEPParameterSpec parameterSpec = new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT
            );
            cipher.init(Cipher.DECRYPT_MODE, privateKey, parameterSpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(passwordCiphertext));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "密码密文无效，请重新输入密码");
        }
    }

    /**
     * 仅供本地和测试环境使用：把明文密码转换成登录接口需要的密文格式，方便 Postman 调试。
     */
    public String encryptForTest(String rawPassword) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            OAEPParameterSpec parameterSpec = new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT
            );
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, parameterSpec);
            byte[] encryptedBytes = cipher.doFinal(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (GeneralSecurityException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "测试密码加密失败");
        }
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            generator.initialize(KEY_SIZE);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("密码传输加密密钥生成失败", exception);
        }
    }
}
