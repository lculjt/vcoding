package com.vcoding.content.domain.topic;

import java.util.Arrays;

public enum ContentType {
    ARTICLE("article"),
    WECHAT_ARTICLE("wechat-article"),
    XIAOHONGSHU_NOTE("xiaohongshu-note"),
    ZHIHU_ANSWER("zhihu-answer"),
    VIDEO_SCRIPT("video-script"),
    MIXED("mixed");

    private final String code;

    ContentType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 将内容类型编码还原为领域枚举。未知编码返回 null，由上层统一抛出业务异常或过滤脏数据。
     */
    public static ContentType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
