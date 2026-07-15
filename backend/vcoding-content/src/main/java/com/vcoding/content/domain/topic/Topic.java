package com.vcoding.content.domain.topic;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 选题领域对象。选题是内容草稿、平台版本、审核和发布任务的业务源头。
 */
public record Topic(
        Long id,
        Long userId,
        String title,
        String contentDirection,
        String targetAudience,
        String keywords,
        List<TargetPlatform> targetPlatforms,
        ContentType contentType,
        ToneStyle toneStyle,
        Integer expectedWordCount,
        TopicStatus status,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
