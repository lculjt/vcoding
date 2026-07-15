package com.vcoding.content.domain.generation;

import java.util.Arrays;

/**
 * AI 内容生成任务类型。与前端生成面板和后续 Agent 模板保持同一套编码。
 */
public enum GenerationTaskType {
    ARTICLE("article"),
    VIDEO_SCRIPT("video-script"),
    OUTLINE("outline"),
    TITLE_CANDIDATES("title-candidates"),
    SUMMARY("summary"),
    TAGS("tags"),
    COVER_PROMPT("cover-prompt");

    private final String code;

    GenerationTaskType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static GenerationTaskType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
