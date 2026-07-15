package com.vcoding.content.api;

import com.vcoding.common.auth.CurrentLoginUser;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.response.ApiResponse;
import com.vcoding.common.response.PageResponse;
import com.vcoding.content.api.dto.CreateTopicRequest;
import com.vcoding.content.api.dto.TopicQueryRequest;
import com.vcoding.content.api.dto.TopicResponse;
import com.vcoding.content.api.dto.UpdateTopicRequest;
import com.vcoding.content.application.topic.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/content/topics")
@RequiredArgsConstructor
@Tag(name = "选题接口", description = "AI 内容平台选题创建、编辑、删除和查询接口")
public class TopicController {
    private final TopicService topicService;

    /**
     * 创建选题。用户身份来自 Gateway 签名头，业务接口不直接解析 JWT。
     */
    @Operation(summary = "创建选题", description = "为当前登录用户创建一个内容选题。")
    @PostMapping
    public ApiResponse<TopicResponse> create(
            @CurrentLoginUser CurrentUser currentUser,
            @Valid @RequestBody CreateTopicRequest request
    ) {
        return ApiResponse.success(topicService.create(currentUser.userId(), request));
    }

    /**
     * 分页查询当前用户自己的选题，避免不同用户之间的数据互相可见。
     */
    @Operation(summary = "查询选题列表", description = "分页查询当前登录用户的选题列表。")
    @GetMapping
    public ApiResponse<PageResponse<TopicResponse>> page(
            @CurrentLoginUser CurrentUser currentUser,
            @Valid TopicQueryRequest request
    ) {
        return ApiResponse.success(topicService.page(currentUser.userId(), request));
    }

    /**
     * 查询选题详情。应用服务会同时校验选题归属，防止越权读取。
     */
    @Operation(summary = "查询选题详情", description = "查询当前登录用户名下的选题详情。")
    @GetMapping("/{id}")
    public ApiResponse<TopicResponse> detail(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long id
    ) {
        return ApiResponse.success(topicService.detail(currentUser.userId(), id));
    }

    /**
     * 更新选题。只允许更新当前用户自己的选题。
     */
    @Operation(summary = "更新选题", description = "更新当前登录用户名下的选题。")
    @PutMapping("/{id}")
    public ApiResponse<TopicResponse> update(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTopicRequest request
    ) {
        return ApiResponse.success(topicService.update(currentUser.userId(), id, request));
    }

    /**
     * 删除选题。接口语义是删除，底层从第一阶段开始使用逻辑删除，保留后续内容链路追溯空间。
     */
    @Operation(summary = "删除选题", description = "删除当前登录用户名下的选题。")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long id
    ) {
        topicService.delete(currentUser.userId(), id);
        return ApiResponse.success();
    }
}
