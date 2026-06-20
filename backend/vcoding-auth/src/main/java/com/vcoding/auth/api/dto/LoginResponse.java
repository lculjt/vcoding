package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "账号密码登录响应")
public record LoginResponse(
        @Schema(description = "当前登录用户")
        CurrentUserResponse user,
        @Schema(description = "登录态有效期，单位秒", example = "7200")
        Long expiresInSeconds
) {
}
