package com.vcoding.content.domain.generation;

/**
 * AI 生成结果载体。不同任务类型只会填充对应字段，其余保持 null。
 */
public record ContentGenerationResult(
        String title,
        String summary,
        String body,
        String scriptContent,
        String coverPrompt,
        String tags,
        String outline,
        String titleCandidates
) {
}
