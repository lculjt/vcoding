package com.vcoding.content.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "更新内容草稿请求")
public class UpdateContentDraftRequest {
    @Schema(description = "草稿标题", example = "AI 编程工具如何提升团队效率")
    @NotBlank(message = "草稿标题不能为空")
    @Size(max = 200, message = "草稿标题不能超过 200 个字符")
    private String title;

    @Schema(description = "内容类型", example = "article")
    @NotBlank(message = "内容类型不能为空")
    @Size(max = 32, message = "内容类型不能超过 32 个字符")
    private String contentType;

    @Schema(description = "摘要")
    @Size(max = 1000, message = "摘要不能超过 1000 个字符")
    private String summary;

    @Schema(description = "正文内容")
    private String body;

    @Schema(description = "短视频脚本")
    private String scriptContent;

    @Schema(description = "封面图提示词")
    @Size(max = 500, message = "封面图提示词不能超过 500 个字符")
    private String coverPrompt;

    @Schema(description = "状态：0草稿，1已定稿", example = "0")
    @Min(value = 0, message = "状态值不能小于 0")
    @Max(value = 1, message = "状态值不能大于 1")
    private Integer status;
}
