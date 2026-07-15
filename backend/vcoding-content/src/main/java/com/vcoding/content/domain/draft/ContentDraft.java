package com.vcoding.content.domain.draft;

import com.vcoding.content.domain.topic.ContentType;

import java.time.LocalDateTime;

/**
 * 内容草稿领域对象。一份选题可以关联多份草稿，草稿是平台适配和发布的直接输入。
 */
public record ContentDraft(
        Long id,
        Long topicId,
        Long userId,
        ContentType contentType,
        String title,
        String summary,
        String body,
        String scriptContent,
        String coverPrompt,
        GenerationSource generationSource,
        DraftStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
