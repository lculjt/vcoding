package com.vcoding.auth.application.captcha;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class CaptchaHashService {

    /**
     * 对验证码进行不可逆哈希，避免 Redis 中保存明文验证码。
     *
     * @param captchaId 图形验证码使用 captchaId，短信验证码使用手机号
     * @param code      用户需要输入的验证码
     */
    public String hash(String captchaId, String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((captchaId + ":" + code).toLowerCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }
}
