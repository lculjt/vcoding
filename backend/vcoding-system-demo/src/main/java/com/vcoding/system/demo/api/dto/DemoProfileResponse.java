package com.vcoding.system.demo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "示例系统当前用户响应")
public record DemoProfileResponse(
        @Schema(description = "系统编码", example = "vcoding-system-demo")
        String systemName,

        @Schema(description = "当前用户 ID", example = "1")
        Long userId,

        @Schema(description = "当前用户名", example = "zhangsan")
        String username,

        @Schema(description = "当前手机号", example = "13800138000")
        String phone,

        @Schema(description = "是否管理员", example = "false")
        boolean adminFlag
) {
}
