package com.vcoding.content.domain.draft;

import java.util.Arrays;

/**
 * 内容草稿状态。阶段 2 先支持编辑中与已定稿，后续生成和审核流程可继续扩展。
 */
public enum DraftStatus {
    DRAFT(0),
    READY(1);

    private final int code;

    DraftStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DraftStatus fromCode(Integer code) {
        if (code == null) {
            return DRAFT;
        }

        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(DRAFT);
    }
}
