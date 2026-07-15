package com.vcoding.content.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "选题列表查询请求")
public class TopicQueryRequest {
    @Schema(description = "当前页码", example = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private Long pageNo = 1L;

    @Schema(description = "每页条数", example = "20")
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Long pageSize = 20L;

    @Schema(description = "标题或关键词搜索", example = "AI")
    @Size(max = 60, message = "搜索关键词不能超过 60 个字符")
    private String keyword;

    @Schema(description = "状态：0草稿，1待生成，2生成中，3待审核，4已完成，5已归档", example = "0")
    @Min(value = 0, message = "状态值不能小于 0")
    @Max(value = 5, message = "状态值不能大于 5")
    private Integer status;

    @Schema(description = "内容类型", example = "article")
    @Size(max = 32, message = "内容类型不能超过 32 个字符")
    private String contentType;
}
