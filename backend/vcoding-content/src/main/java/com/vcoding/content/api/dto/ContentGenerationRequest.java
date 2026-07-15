package com.vcoding.content.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "AI 内容生成请求")
public class ContentGenerationRequest {
    @Schema(description = "任务类型：article、video-script、outline、title-candidates、summary、tags、cover-prompt", example = "article")
    @NotBlank(message = "任务类型不能为空")
    @Size(max = 32, message = "任务类型不能超过 32 个字符")
    private String taskType;
}
