package com.vcoding.common.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CurrentLoginUserWebMvcConfig implements WebMvcConfigurer {
    private final CurrentLoginUserArgumentResolver currentLoginUserArgumentResolver;

    /**
     * 注册当前登录用户参数解析器，供所有 Spring MVC 业务模块复用。
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentLoginUserArgumentResolver);
    }
}
