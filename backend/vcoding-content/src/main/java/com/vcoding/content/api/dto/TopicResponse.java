package com.vcoding.content.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "选题响应")
public record TopicResponse(
        @Schema(description = "选题 ID", example = "1")
        Long id,
        @Schema(description = "创建人用户 ID", example = "1")
        Long userId,
        @Schema(description = "选题标题", example = "AI 编程工具如何提升中小团队效率")
        String title,
        @Schema(description = "内容方向", example = "AI 工具实践")
        String contentDirection,
        @Schema(description = "目标受众", example = "中小团队技术负责人")
        String targetAudience,
        @Schema(description = "核心关键词，第一阶段使用逗号分隔", example = "AI 编程,Codex,效率")
        String keywords,
        @Schema(description = "目标平台编码列表", example = "[\"wechat\",\"xiaohongshu\",\"zhihu\"]")
        List<String> targetPlatforms,
        @Schema(description = "内容类型", example = "article")
        String contentType,
        @Schema(description = "语气风格", example = "professional")
        String toneStyle,
        @Schema(description = "期望字数", example = "1500")
        Integer expectedWordCount,
        @Schema(description = "状态编码", example = "0")
        Integer status,
        @Schema(description = "备注", example = "先生成公众号长文，再适配小红书")
        String remark,
        @Schema(description = "创建时间")
        LocalDateTime createdAt,
        @Schema(description = "更新时间")
        LocalDateTime updatedAt
) {
}
