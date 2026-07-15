package com.vcoding.content.api;

import com.vcoding.common.auth.CurrentLoginUser;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.response.ApiResponse;
import com.vcoding.content.api.dto.ContentGenerationRequest;
import com.vcoding.content.api.dto.ContentGenerationResponse;
import com.vcoding.content.application.generation.ContentGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Tag(name = "AI 内容生成接口", description = "基于选题触发 AI 生成，并保存运行记录与草稿")
public class ContentGenerationController {
    private final ContentGenerationService contentGenerationService;

    @Operation(summary = "触发 AI 生成", description = "基于选题触发指定类型的 AI 生成任务。")
    @PostMapping("/topics/{topicId}/generate")
    public ApiResponse<ContentGenerationResponse> generate(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long topicId,
            @Valid @RequestBody ContentGenerationRequest request
    ) {
        return ApiResponse.success(contentGenerationService.generate(currentUser.userId(), topicId, request));
    }

    @Operation(summary = "重试失败任务", description = "基于失败运行记录重新触发同类型 AI 生成。")
    @PostMapping("/generation-runs/{runId}/retry")
    public ApiResponse<ContentGenerationResponse> retry(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long runId
    ) {
        return ApiResponse.success(contentGenerationService.retry(currentUser.userId(), runId));
    }

    @Operation(summary = "查询选题生成记录", description = "查询指定选题最近 20 条 AI 生成运行记录。")
    @GetMapping("/topics/{topicId}/generation-runs")
    public ApiResponse<List<ContentGenerationResponse>> listByTopic(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long topicId
    ) {
        return ApiResponse.success(contentGenerationService.listByTopic(currentUser.userId(), topicId));
    }

    @Operation(summary = "查询生成记录详情", description = "查询单条 AI 生成运行记录详情。")
    @GetMapping("/generation-runs/{runId}")
    public ApiResponse<ContentGenerationResponse> detail(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long runId
    ) {
        return ApiResponse.success(contentGenerationService.detail(currentUser.userId(), runId));
    }
}
