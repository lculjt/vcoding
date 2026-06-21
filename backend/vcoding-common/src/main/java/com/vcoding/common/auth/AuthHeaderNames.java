package com.vcoding.common.auth;

/**
 * Gateway 与内部服务之间传递用户身份的请求头名称。所有入口都应先清理外部伪造头。
 */
public final class AuthHeaderNames {
    public static final String GATEWAY_SIGNATURE = "X-Vcoding-Gateway-Signature";
    public static final String GATEWAY_TIMESTAMP = "X-Vcoding-Gateway-Timestamp";
    public static final String USER_ID = "X-Vcoding-User-Id";
    public static final String USERNAME = "X-Vcoding-Username";
    public static final String PHONE = "X-Vcoding-Phone";
    public static final String ADMIN = "X-Vcoding-Admin";

    private AuthHeaderNames() {
    }
}
