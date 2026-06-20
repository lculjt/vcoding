package com.vcoding.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * 配置统一用户中心接口文档的基础信息，具体接口说明由 Controller 注解补充。
     */
    @Bean
    public OpenAPI vcodingAuthOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("vcoding 统一用户中心接口")
                        .version("0.1.0")
                        .description("统一登录、验证码、注册和后续账号体系相关接口"));
    }
}
