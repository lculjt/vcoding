package com.vcoding.content.domain.topic;

import java.util.Arrays;

/**
 * 内容发布目标平台。与后续平台适配、发布任务中的平台编码保持一致。
 */
public enum TargetPlatform {
    WECHAT("wechat"),
    XIAOHONGSHU("xiaohongshu"),
    ZHIHU("zhihu"),
    DOUYIN("douyin"),
    BILIBILI("bilibili");

    private final String code;

    TargetPlatform(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 将平台编码还原为领域枚举。未知编码返回 null，由上层统一抛出业务异常。
     */
    public static TargetPlatform fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(platform -> platform.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
