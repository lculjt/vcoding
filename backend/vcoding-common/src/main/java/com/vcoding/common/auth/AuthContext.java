package com.vcoding.common.auth;

import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;

/**
 * 基于 ThreadLocal 保存当前请求用户，供 MVC 业务接口和应用服务读取。
 */
public final class AuthContext {
    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    public static CurrentUser get() {
        return CURRENT_USER.get();
    }

    public static CurrentUser requireLogin() {
        CurrentUser currentUser = get();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.AUTH_NOT_LOGIN);
        }
        return currentUser;
    }

    public static CurrentUser requireAdmin() {
        CurrentUser currentUser = requireLogin();
        if (!currentUser.adminFlag()) {
            throw new BusinessException(ErrorCode.COMMON_FORBIDDEN);
        }
        return currentUser;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
