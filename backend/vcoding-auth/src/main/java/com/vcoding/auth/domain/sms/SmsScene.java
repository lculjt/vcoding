package com.vcoding.auth.domain.sms;

import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;

import java.util.Arrays;

public enum SmsScene {
    REGISTER("register"),
    LOGIN("login"),
    RESET_PASSWORD("reset-password"),
    BIND_PHONE("bind-phone"),
    CHANGE_PHONE("change-phone");

    private final String code;

    SmsScene(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 将前端传入的场景编码转换为枚举，避免业务流程使用未定义的短信场景。
     */
    public static SmsScene fromCode(String code) {
        return Arrays.stream(values())
                .filter(scene -> scene.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_UNSUPPORTED_SMS_SCENE));
    }
}
