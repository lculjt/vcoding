package com.vcoding.auth.api;

import com.vcoding.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "健康检查", description = "服务存活状态检查接口")
public class HealthController {

    @Operation(summary = "认证服务健康检查", description = "用于确认 vcoding-auth 服务是否正常启动。")
    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of(
                "service", "vcoding-auth",
                "status", "UP"
        ));
    }
}
