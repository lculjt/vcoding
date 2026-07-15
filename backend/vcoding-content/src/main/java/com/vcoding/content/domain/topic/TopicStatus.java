package com.vcoding.content.domain.topic;

import java.util.Arrays;

public enum TopicStatus {
    DRAFT(0),
    PENDING_GENERATE(1),
    GENERATING(2),
    PENDING_REVIEW(3),
    COMPLETED(4),
    ARCHIVED(5);

    private final int code;

    TopicStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 根据数据库状态值还原选题状态，未知状态按草稿处理，避免错误状态进入生成流程。
     */
    public static TopicStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(DRAFT);
    }
}
