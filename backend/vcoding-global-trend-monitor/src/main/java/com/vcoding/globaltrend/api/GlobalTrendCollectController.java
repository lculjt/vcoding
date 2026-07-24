package com.vcoding.globaltrend.api;

import com.vcoding.common.auth.CurrentLoginUser;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ApiResponse;
import com.vcoding.common.response.ErrorCode;
import com.vcoding.globaltrend.application.collect.GlobalTrendCollectService;
import com.vcoding.globaltrend.domain.collect.CollectJobResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/global-trend/sources")
@RequiredArgsConstructor
@Tag(name = "热点采集", description = "管理员手动触发首批海外热点数据源采集")
public class GlobalTrendCollectController {
    private final GlobalTrendCollectService collectService;

    @PostMapping("/{code}/collect")
    @Operation(summary = "手动采集数据源", description = "管理员触发单个数据源采集，并返回任务统计结果")
    public ApiResponse<CollectJobResult> collect(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable String code
    ) {
        requireAdmin(currentUser);
        return ApiResponse.success(collectService.collect(code, "MANUAL"));
    }

    private void requireAdmin(CurrentUser currentUser) {
        if (!currentUser.adminFlag()) {
            throw new BusinessException(ErrorCode.COMMON_FORBIDDEN, "只有管理员可以触发采集");
        }
    }
}
