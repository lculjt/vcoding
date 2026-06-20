package com.vcoding.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vcoding.auth")
public class AuthProperties {
    /**
     * 统一登录态 Cookie 名称，前端只依赖该 Cookie 是否随请求自动携带。
     */
    private String cookieName = "VCODING_TOKEN";

    /**
     * JWT 签发方，用于避免其他系统签发的 Token 被误用到当前用户中心。
     */
    private String jwtIssuer = "vcoding";

    /**
     * JWT HMAC 密钥。生产环境必须通过环境变量覆盖，不能使用本地默认值。
     */
    private String jwtSecret;

    /**
     * 登录态有效期，单位秒。
     */
    private long jwtTtlSeconds = 7200;

    /**
     * Cookie 生效路径，统一登录场景默认覆盖整个域名。
     */
    private String cookiePath = "/";

    /**
     * 生产 HTTPS 环境应开启 secure，本地 HTTP 调试保持 false。
     */
    private boolean cookieSecure = false;

    /**
     * Cookie SameSite 策略，第一阶段本地同源代理使用 Lax 即可。
     */
    private String cookieSameSite = "Lax";
}
