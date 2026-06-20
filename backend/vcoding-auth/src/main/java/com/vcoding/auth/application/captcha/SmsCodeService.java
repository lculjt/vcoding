package com.vcoding.auth.application.captcha;

import com.vcoding.auth.api.dto.SendSmsCodeRequest;
import com.vcoding.auth.api.dto.SendSmsCodeResponse;
import com.vcoding.auth.config.CaptchaProperties;
import com.vcoding.auth.domain.sms.SmsScene;
import com.vcoding.auth.domain.sms.SmsSender;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SmsCodeService {
    private static final String SMS_CODE_KEY_PREFIX = "captcha:sms:";
    private static final String SMS_COOLDOWN_KEY_PREFIX = "captcha:sms:cooldown:";
    private static final String SMS_PHONE_LIMIT_KEY_PREFIX = "captcha:sms:limit:phone:";
    private static final String SMS_IP_LIMIT_KEY_PREFIX = "captcha:sms:limit:ip:";
    private static final String SMS_ATTEMPT_KEY_PREFIX = "captcha:sms:attempt:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final CaptchaProperties captchaProperties;
    private final CaptchaHashService captchaHashService;
    private final ImageCaptchaService imageCaptchaService;
    private final SmsSender smsSender;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 发送短信验证码。发送前必须先通过图形验证码，再做手机号和 IP 维度限流。
     */
    public SendSmsCodeResponse sendCode(SendSmsCodeRequest request, String clientIp) {
        SmsScene scene = SmsScene.fromCode(request.getScene());
        imageCaptchaService.verifyAndDelete(request.getCaptchaId(), request.getCaptchaCode());

        String phone = request.getPhone();
        checkCooldown(scene, phone);
        checkDailyLimit(phoneLimitKey(scene, phone), captchaProperties.getSmsDailyPhoneLimit());
        checkDailyLimit(ipLimitKey(scene, clientIp), captchaProperties.getSmsDailyIpLimit());

        String code = randomNumericCode(captchaProperties.getSmsCodeLength());
        String codeKey = smsCodeKey(scene, phone);
        String attemptKey = attemptKey(scene, phone);
        String codeHash = captchaHashService.hash(phone, code);

        // Redis 只保存验证码哈希，真实验证码只交给短信发送通道。
        redisTemplate.opsForValue().set(
                codeKey,
                codeHash,
                Duration.ofSeconds(captchaProperties.getSmsTtlSeconds())
        );
        // 新验证码生成后清理旧失败次数，避免旧失败记录影响新验证码。
        redisTemplate.delete(attemptKey);
        redisTemplate.opsForValue().set(
                cooldownKey(scene, phone),
                "1",
                Duration.ofSeconds(captchaProperties.getSmsCooldownSeconds())
        );
        incrementDailyLimit(phoneLimitKey(scene, phone));
        incrementDailyLimit(ipLimitKey(scene, clientIp));

        smsSender.sendSmsCode(phone, scene, code);

        return new SendSmsCodeResponse(
                scene.getCode(),
                phone,
                captchaProperties.getSmsTtlSeconds(),
                captchaProperties.getSmsCooldownSeconds()
        );
    }

    /**
     * 校验短信验证码并在成功后删除。该方法后续会被注册、登录、找回密码等流程复用。
     */
    public void verifyAndDelete(SmsScene scene, String phone, String code) {
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_SMS_CODE);
        }

        String codeKey = smsCodeKey(scene, phone);
        String attemptKey = attemptKey(scene, phone);
        String storedHash = redisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(storedHash)) {
            throw new BusinessException(ErrorCode.AUTH_SMS_CODE_EXPIRED);
        }

        String inputHash = captchaHashService.hash(phone, code);
        if (!storedHash.equals(inputHash)) {
            incrementVerifyAttempt(attemptKey);
            throw new BusinessException(ErrorCode.AUTH_INVALID_SMS_CODE);
        }

        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptKey);
    }

    /**
     * 检查短信发送冷却窗口，防止用户频繁点击发送按钮。
     */
    private void checkCooldown(SmsScene scene, String phone) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(scene, phone)))) {
            throw new BusinessException(ErrorCode.AUTH_SMS_SEND_COOLDOWN);
        }
    }

    /**
     * 检查单日发送上限。手机号维度限制单个账号，IP 维度限制同一来源的批量请求。
     */
    private void checkDailyLimit(String key, int limit) {
        String current = redisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(current) && Long.parseLong(current) >= limit) {
            throw new BusinessException(ErrorCode.AUTH_SMS_SEND_LIMIT_EXCEEDED);
        }
    }

    /**
     * 记录当日发送次数。首次写入时设置过期时间，避免 Redis 长期保留历史计数。
     */
    private void incrementDailyLimit(String key) {
        Long current = redisTemplate.opsForValue().increment(key);
        if (current != null && current == 1L) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }
    }

    /**
     * 记录短信验证码校验失败次数。超过阈值后删除验证码，要求用户重新发起获取流程。
     */
    private void incrementVerifyAttempt(String key) {
        Long current = redisTemplate.opsForValue().increment(key);
        if (current != null && current == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(captchaProperties.getSmsTtlSeconds()));
        }
        if (current != null && current >= captchaProperties.getSmsVerifyFailLimit()) {
            redisTemplate.delete(key.replace(SMS_ATTEMPT_KEY_PREFIX, SMS_CODE_KEY_PREFIX));
            throw new BusinessException(ErrorCode.AUTH_SMS_VERIFY_LIMIT_EXCEEDED);
        }
    }

    /**
     * 生成数字验证码。使用 SecureRandom 避免普通 Random 带来的可预测性。
     */
    private String randomNumericCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }

    /**
     * 短信验证码内容 key，按场景和手机号隔离，避免登录验证码误用于注册等场景。
     */
    private String smsCodeKey(SmsScene scene, String phone) {
        return SMS_CODE_KEY_PREFIX + scene.getCode() + ":" + phone;
    }

    /**
     * 短信发送冷却 key。
     */
    private String cooldownKey(SmsScene scene, String phone) {
        return SMS_COOLDOWN_KEY_PREFIX + scene.getCode() + ":" + phone;
    }

    /**
     * 手机号维度的每日发送次数 key。
     */
    private String phoneLimitKey(SmsScene scene, String phone) {
        return SMS_PHONE_LIMIT_KEY_PREFIX + scene.getCode() + ":" + phone + ":" + today();
    }

    /**
     * IP 维度的每日发送次数 key。
     */
    private String ipLimitKey(SmsScene scene, String clientIp) {
        return SMS_IP_LIMIT_KEY_PREFIX + scene.getCode() + ":" + clientIp + ":" + today();
    }

    /**
     * 短信验证码失败次数 key。
     */
    private String attemptKey(SmsScene scene, String phone) {
        return SMS_ATTEMPT_KEY_PREFIX + scene.getCode() + ":" + phone;
    }

    /**
     * 用 yyyyMMdd 作为每日限流后缀，便于按天自然隔离计数。
     */
    private String today() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
