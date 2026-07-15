package com.vcoding.content.api;

import com.vcoding.common.auth.CurrentLoginUser;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.response.ApiResponse;
import com.vcoding.content.api.dto.ContentDraftResponse;
import com.vcoding.content.api.dto.CreateContentDraftRequest;
import com.vcoding.content.api.dto.UpdateContentDraftRequest;
import com.vcoding.content.application.draft.ContentDraftService;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Tag(name = "内容草稿接口", description = "AI 内容平台内容草稿创建、编辑、删除和查询接口")
public class ContentDraftController {
    private final ContentDraftService contentDraftService;

    @Operation(summary = "创建内容草稿", description = "在指定选题下创建一份人工内容草稿。")
    @PostMapping("/topics/{topicId}/drafts")
    public ApiResponse<ContentDraftResponse> create(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long topicId,
            @Valid @RequestBody CreateContentDraftRequest request
    ) {
        return ApiResponse.success(contentDraftService.create(currentUser.userId(), topicId, request));
    }

    @Operation(summary = "查询选题草稿列表", description = "查询指定选题下的全部内容草稿。")
    @GetMapping("/topics/{topicId}/drafts")
    public ApiResponse<List<ContentDraftResponse>> listByTopic(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long topicId
    ) {
        return ApiResponse.success(contentDraftService.listByTopic(currentUser.userId(), topicId));
    }

    @Operation(summary = "查询草稿详情", description = "查询当前登录用户名下的内容草稿详情。")
    @GetMapping("/drafts/{id}")
    public ApiResponse<ContentDraftResponse> detail(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long id
    ) {
        return ApiResponse.success(contentDraftService.detail(currentUser.userId(), id));
    }

    @Operation(summary = "更新内容草稿", description = "更新当前登录用户名下的内容草稿。")
    @PutMapping("/drafts/{id}")
    public ApiResponse<ContentDraftResponse> update(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateContentDraftRequest request
    ) {
        return ApiResponse.success(contentDraftService.update(currentUser.userId(), id, request));
    }

    @Operation(summary = "删除内容草稿", description = "删除当前登录用户名下的内容草稿。")
    @DeleteMapping("/drafts/{id}")
    public ApiResponse<Void> delete(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable Long id
    ) {
        contentDraftService.delete(currentUser.userId(), id);
        return ApiResponse.success();
    }
}
