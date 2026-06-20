package com.vcoding.auth.infrastructure.sms;

import com.vcoding.auth.domain.sms.SmsScene;
import com.vcoding.auth.domain.sms.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "vcoding.sms", name = "provider", havingValue = "console", matchIfMissing = true)
public class ConsoleSmsSender implements SmsSender {

    /**
     * 本地开发短信实现。生产环境应替换为真实短信供应商，不能在日志中输出完整验证码。
     */
    @Override
    public void sendSmsCode(String phone, SmsScene scene, String code) {
        log.info("本地短信验证码: phone={}, scene={}, code={}", maskPhone(phone), scene.getCode(), code);
    }

    /**
     * 日志中隐藏手机号中间四位，避免本地排查时泄露完整手机号。
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
