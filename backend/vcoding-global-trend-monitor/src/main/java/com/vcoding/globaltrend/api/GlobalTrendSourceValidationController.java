package com.vcoding.globaltrend.api;

import com.vcoding.common.auth.CurrentLoginUser;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ApiResponse;
import com.vcoding.common.response.ErrorCode;
import com.vcoding.globaltrend.application.sourcevalidation.SourceValidationService;
import com.vcoding.globaltrend.domain.sourcevalidation.SourceValidationResult;
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
@Tag(name = "数据源验证", description = "验证首批海外热点数据源的认证、字段和限流状态")
public class GlobalTrendSourceValidationController {
    private final SourceValidationService sourceValidationService;

    @PostMapping("/{code}/test")
    @Operation(summary = "测试数据源", description = "管理员触发少量公开数据读取，不写入热点表")
    public ApiResponse<SourceValidationResult> test(
            @CurrentLoginUser CurrentUser currentUser,
            @PathVariable String code
    ) {
        requireAdmin(currentUser);
        return ApiResponse.success(sourceValidationService.validate(code));
    }

    private void requireAdmin(CurrentUser currentUser) {
        if (!currentUser.adminFlag()) {
            throw new BusinessException(ErrorCode.COMMON_FORBIDDEN, "只有管理员可以测试数据源");
        }
    }
}
