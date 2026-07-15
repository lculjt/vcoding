package com.vcoding.content.domain.topic;

import java.util.Arrays;

public enum ToneStyle {
    PROFESSIONAL("professional"),
    CASUAL("casual"),
    MARKETING("marketing"),
    EDUCATIONAL("educational"),
    REVIEW("review"),
    STORY("story");

    private final String code;

    ToneStyle(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 将语气风格编码还原为领域枚举。未知编码返回 null，由上层统一抛出业务异常或过滤脏数据。
     */
    public static ToneStyle fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(style -> style.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
