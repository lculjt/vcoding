package com.vcoding.system.demo.config;

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
     * 业务系统内部第二层校验：所有 demo 业务接口必须来自可信 Gateway。
     */
    @Bean
    public FilterRegistrationBean<InternalGatewayUserFilter> internalGatewayUserFilter() {
        FilterRegistrationBean<InternalGatewayUserFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new InternalGatewayUserFilter(gatewayUserHeaderService, objectMapper));
        registrationBean.addUrlPatterns("/api/demo/*");
        registrationBean.setOrder(10);
        return registrationBean;
    }
}
