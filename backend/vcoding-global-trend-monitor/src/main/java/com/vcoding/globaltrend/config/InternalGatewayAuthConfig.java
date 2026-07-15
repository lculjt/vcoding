package com.vcoding.globaltrend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcoding.common.auth.GatewayUserHeaderService;
import com.vcoding.common.auth.InternalGatewayUserFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InternalGatewayAuthConfig {
    private final GatewayUserHeaderService gatewayUserHeaderService;
    private final ObjectMapper objectMapper;

    /**
     * 业务模块只接受可信 Gateway 转发的请求，避免外部请求伪造当前用户身份。
     */
    @Bean
    public FilterRegistrationBean<InternalGatewayUserFilter> internalGatewayUserFilter() {
        FilterRegistrationBean<InternalGatewayUserFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new InternalGatewayUserFilter(gatewayUserHeaderService, objectMapper));
        registrationBean.addUrlPatterns("/api/global-trend/*");
        registrationBean.setOrder(10);
        return registrationBean;
    }
}
