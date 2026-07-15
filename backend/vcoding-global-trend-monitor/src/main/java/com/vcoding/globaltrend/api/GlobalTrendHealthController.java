package com.vcoding.globaltrend.api;

import com.vcoding.common.auth.CurrentLoginUser;
import com.vcoding.common.auth.CurrentUser;
import com.vcoding.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/global-trend")
public class GlobalTrendHealthController {

    /**
     * 基础接口用于验证 Gateway 登录态、内部用户头和业务模块路由已经连通。
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health(@CurrentLoginUser CurrentUser currentUser) {
        return ApiResponse.success(Map.of(
                "system", "vcoding-global-trend-monitor",
                "status", "ready",
                "userId", currentUser.userId()
        ));
    }
}
