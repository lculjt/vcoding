package com.vcoding.auth.application.captcha;

import com.vcoding.auth.api.dto.ImageCaptchaResponse;
import com.vcoding.auth.config.CaptchaProperties;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageCaptchaService {
    private static final String IMAGE_CAPTCHA_KEY_PREFIX = "captcha:image:";
    private static final char[] IMAGE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final CaptchaProperties captchaProperties;
    private final CaptchaHashService captchaHashService;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 创建一次性图形验证码，并把验证码哈希写入 Redis。
     */
    public ImageCaptchaResponse createImageCaptcha() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode(captchaProperties.getImageCodeLength(), IMAGE_CODE_CHARS);
        String key = imageCaptchaKey(captchaId);
        String codeHash = captchaHashService.hash(captchaId, code);

        redisTemplate.opsForValue().set(
                key,
                codeHash,
                Duration.ofSeconds(captchaProperties.getImageTtlSeconds())
        );

        return new ImageCaptchaResponse(
                captchaId,
                toSvgDataUrl(code),
                captchaProperties.getImageTtlSeconds()
        );
    }

    /**
     * 校验图形验证码并立即删除，防止同一个 captchaId 被重复用于多次短信发送。
     */
    public void verifyAndDelete(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_IMAGE_CAPTCHA);
        }

        String key = imageCaptchaKey(captchaId);
        String storedHash = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(storedHash)) {
            throw new BusinessException(ErrorCode.AUTH_IMAGE_CAPTCHA_EXPIRED);
        }

        String inputHash = captchaHashService.hash(captchaId, captchaCode);
        redisTemplate.delete(key);

        // 无论校验成功或失败都删除验证码，避免暴力试错。
        if (!storedHash.equals(inputHash)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_IMAGE_CAPTCHA);
        }
    }

    /**
     * 生成不易混淆的随机验证码字符，例如排除了 I、O、0、1。
     */
    private String randomCode(int length, char[] chars) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(chars[secureRandom.nextInt(chars.length)]);
        }
        return builder.toString();
    }

    /**
     * 直接返回 SVG data URL，第一阶段不引入额外图片验证码依赖，便于前端直接展示。
     */
    private String toSvgDataUrl(String code) {
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="120" height="40" viewBox="0 0 120 40">
                  <rect width="120" height="40" rx="6" fill="#f8fafc"/>
                  <path d="M8 28 C30 8, 52 34, 74 14 S104 18, 112 9" stroke="#cbd5e1" stroke-width="2" fill="none"/>
                  <text x="60" y="27" text-anchor="middle" font-family="Arial, sans-serif" font-size="22" font-weight="700" letter-spacing="4" fill="#1f2937">%s</text>
                </svg>
                """.formatted(code);
        String encoded = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + encoded;
    }

    /**
     * 统一 Redis key 格式，便于后续排查和设置清理策略。
     */
    private String imageCaptchaKey(String captchaId) {
        return IMAGE_CAPTCHA_KEY_PREFIX + captchaId;
    }
}
