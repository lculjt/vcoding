package com.vcoding.content.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "创建选题请求")
public class CreateTopicRequest {
    @Schema(description = "选题标题", example = "AI 编程工具如何提升中小团队效率")
    @NotBlank(message = "选题标题不能为空")
    @Size(max = 120, message = "选题标题不能超过 120 个字符")
    private String title;

    @Schema(description = "内容方向", example = "AI 工具实践")
    @Size(max = 120, message = "内容方向不能超过 120 个字符")
    private String contentDirection;

    @Schema(description = "目标受众", example = "中小团队技术负责人")
    @Size(max = 120, message = "目标受众不能超过 120 个字符")
    private String targetAudience;

    @Schema(description = "核心关键词，第一阶段使用逗号分隔", example = "AI 编程,Codex,效率")
    @Size(max = 500, message = "核心关键词不能超过 500 个字符")
    private String keywords;

    @Schema(description = "目标平台编码列表", example = "[\"wechat\",\"xiaohongshu\",\"zhihu\"]")
    @Size(max = 5, message = "目标平台不能超过 5 个")
    private List<String> targetPlatforms;

    @Schema(description = "内容类型：article、wechat-article、xiaohongshu-note、zhihu-answer、video-script、mixed", example = "article")
    @NotBlank(message = "内容类型不能为空")
    @Size(max = 32, message = "内容类型不能超过 32 个字符")
    private String contentType;

    @Schema(description = "语气风格：professional、casual、marketing、educational、review、story", example = "professional")
    @Size(max = 32, message = "语气风格不能超过 32 个字符")
    private String toneStyle;

    @Schema(description = "期望字数", example = "1500")
    @Min(value = 100, message = "期望字数不能小于 100")
    @Max(value = 50000, message = "期望字数不能超过 50000")
    private Integer expectedWordCount;

    @Schema(description = "备注", example = "先生成公众号长文，再适配小红书")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;
}
