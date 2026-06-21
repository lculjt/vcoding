package com.vcoding.common.auth;

/**
 * 当前登录用户的最小身份信息。业务模块只依赖该对象，不直接解析 JWT 或 Cookie。
 */
public record CurrentUser(
        Long userId,
        String username,
        String phone,
        boolean adminFlag
) {
}
