package com.vcoding.content.domain.draft;

import java.util.Arrays;

/**
 * 内容草稿生成来源。阶段 2 仅使用 manual，ai 预留给阶段 3。
 */
public enum GenerationSource {
    MANUAL("manual"),
    AI("ai");

    private final String code;

    GenerationSource(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static GenerationSource fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(source -> source.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
