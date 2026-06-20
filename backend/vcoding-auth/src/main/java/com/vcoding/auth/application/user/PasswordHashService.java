package com.vcoding.auth.application.user;

import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Service
public class PasswordHashService {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String FORMAT_PREFIX = "pbkdf2_sha256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 生成带算法、迭代次数和盐值的密码哈希，数据库中不保存明文密码。
     */
    public String hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS, HASH_BITS);
        return FORMAT_PREFIX + "$"
                + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 校验用户输入的密码。该方法预留给下一步账号密码登录接口复用。
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 4 || !FORMAT_PREFIX.equals(parts[0])) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_PASSWORD_HASH);
        }

        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
        byte[] inputHash = pbkdf2(rawPassword.toCharArray(), salt, iterations, expectedHash.length * 8);

        return constantTimeEquals(expectedHash, inputHash);
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int hashBits) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, hashBits);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("密码哈希算法不可用", exception);
        }
    }

    /**
     * 使用固定时间比较，降低通过响应时间推测密码哈希内容的风险。
     */
    private boolean constantTimeEquals(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            return false;
        }

        int result = 0;
        for (int index = 0; index < expected.length; index++) {
            result |= expected[index] ^ actual[index];
        }
        return result == 0;
    }
}
