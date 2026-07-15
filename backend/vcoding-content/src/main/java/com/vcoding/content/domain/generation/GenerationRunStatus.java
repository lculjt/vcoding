package com.vcoding.content.domain.generation;

import java.util.Arrays;

/**
 * AI 生成运行状态。
 */
public enum GenerationRunStatus {
    RUNNING(0),
    SUCCESS(1),
    FAILED(2);

    private final int code;

    GenerationRunStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static GenerationRunStatus fromCode(Integer code) {
        if (code == null) {
            return RUNNING;
        }

        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(RUNNING);
    }
}
