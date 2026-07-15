package com.vcoding.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "用户注册请求")
public class RegisterRequest {
    @Schema(description = "用户名，必须以字母开头，只能包含字母、数字和下划线", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度必须在 4 到 32 位之间")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "用户名必须以字母开头，只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "中国大陆手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Schema(description = "Base64 编码的 RSA-OAEP 密码密文", example = "m3n...")
    @NotBlank(message = "密码密文不能为空")
    @Size(max = 512, message = "密码密文长度不能超过 512 位")
    private String passwordCiphertext;

    @Schema(description = "密码加密公钥 ID", example = "202606260001")
    @NotBlank(message = "密码加密公钥 ID 不能为空")
    private String passwordKeyId;

    @Schema(description = "注册场景短信验证码", example = "123456")
    @NotBlank(message = "短信验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "短信验证码必须是 6 位数字")
    private String smsCode;
}
