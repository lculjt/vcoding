package com.vcoding.common.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vcoding.gateway")
public class GatewayInternalProperties {
    /**
     * Gateway 与内部服务共享的 HMAC 密钥，用于防止外部请求伪造内部用户头。
     */
    private String internalSecret = "vcoding-local-gateway-internal-secret-change-before-production-2026";

    /**
     * 内部用户头签名有效期，单位秒。过期请求会被业务服务拒绝。
     */
    private long internalTimestampTtlSeconds = 120;
}
