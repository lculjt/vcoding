package com.vcoding.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vcoding.captcha")
public class CaptchaProperties {
    /**
     * 图形验证码字符长度。
     */
    private int imageCodeLength = 4;
    /**
     * 图形验证码有效期，单位秒。
     */
    private long imageTtlSeconds = 300;
    /**
     * 短信验证码数字长度。
     */
    private int smsCodeLength = 6;
    /**
     * 短信验证码有效期，单位秒。
     */
    private long smsTtlSeconds = 300;
    /**
     * 同手机号同场景再次发送短信验证码的冷却时间，单位秒。
     */
    private long smsCooldownSeconds = 60;
    /**
     * 同手机号同场景每日发送上限。
     */
    private int smsDailyPhoneLimit = 10;
    /**
     * 同 IP 同场景每日发送上限。
     */
    private int smsDailyIpLimit = 50;
    /**
     * 单个短信验证码允许的最大校验失败次数。
     */
    private int smsVerifyFailLimit = 5;
}
