package com.vcoding.content.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "AI 内容生成响应")
public record ContentGenerationResponse(
        @Schema(description = "运行记录 ID", example = "1")
        Long runId,
        @Schema(description = "选题 ID", example = "1")
        Long topicId,
        @Schema(description = "关联草稿 ID，仅文章和脚本任务自动生成草稿时返回")
        Long draftId,
        @Schema(description = "任务类型", example = "article")
        String taskType,
        @Schema(description = "运行状态：running、success、failed")
        String status,
        @Schema(description = "生成标题")
        String title,
        @Schema(description = "生成摘要")
        String summary,
        @Schema(description = "生成正文")
        String body,
        @Schema(description = "生成脚本")
        String scriptContent,
        @Schema(description = "封面图提示词")
        String coverPrompt,
        @Schema(description = "标签，逗号分隔")
        String tags,
        @Schema(description = "大纲")
        String outline,
        @Schema(description = "标题候选")
        String titleCandidates,
        @Schema(description = "失败原因")
        String errorMessage,
        @Schema(description = "模型名称")
        String modelName,
        @Schema(description = "执行耗时毫秒")
        Long durationMs,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}
