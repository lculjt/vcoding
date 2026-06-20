package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "图形验证码响应")
public class ImageCaptchaResponse {
    @Schema(description = "图形验证码 ID，发送短信验证码时需要回传", example = "f3b0f6a5c5d04ff3a14e8a9d1c6b7e01")
    private final String captchaId;

    @Schema(description = "SVG 图片 data URL，前端可直接放到 img src 中展示")
    private final String imageDataUrl;

    @Schema(description = "图形验证码有效期，单位秒", example = "300")
    private final long expiresInSeconds;
}
