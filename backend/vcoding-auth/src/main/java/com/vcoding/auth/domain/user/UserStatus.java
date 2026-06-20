package com.vcoding.auth.domain.user;

import java.util.Arrays;

public enum UserStatus {
    ENABLED(1),
    DISABLED(0);

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 根据数据库状态值还原用户状态，后续登录鉴权会复用该转换。
     */
    public static UserStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(DISABLED);
    }
}
