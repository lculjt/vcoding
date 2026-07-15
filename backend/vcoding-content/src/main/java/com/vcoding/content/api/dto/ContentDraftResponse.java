package com.vcoding.content.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "内容草稿响应")
public record ContentDraftResponse(
        @Schema(description = "草稿 ID", example = "1")
        Long id,
        @Schema(description = "所属选题 ID", example = "1")
        Long topicId,
        @Schema(description = "创建人用户 ID", example = "1")
        Long userId,
        @Schema(description = "内容类型", example = "article")
        String contentType,
        @Schema(description = "草稿标题")
        String title,
        @Schema(description = "摘要")
        String summary,
        @Schema(description = "正文内容")
        String body,
        @Schema(description = "短视频脚本")
        String scriptContent,
        @Schema(description = "封面图提示词")
        String coverPrompt,
        @Schema(description = "生成来源：manual、ai", example = "manual")
        String generationSource,
        @Schema(description = "状态：0草稿，1已定稿", example = "0")
        Integer status,
        @Schema(description = "创建时间")
        LocalDateTime createdAt,
        @Schema(description = "更新时间")
        LocalDateTime updatedAt
) {
}
