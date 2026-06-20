package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户注册响应")
public record RegisterResponse(
        @Schema(description = "用户 ID", example = "1")
        Long userId,
        @Schema(description = "用户名", example = "zhangsan")
        String username,
        @Schema(description = "手机号", example = "13800138000")
        String phone,
        @Schema(description = "是否管理员", example = "false")
        Boolean adminFlag
) {
}
