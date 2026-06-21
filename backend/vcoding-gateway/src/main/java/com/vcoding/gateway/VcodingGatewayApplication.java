package com.vcoding.gateway;

import com.vcoding.common.auth.GatewayUserHeaderService;
import com.vcoding.common.auth.JwtService;
import com.vcoding.common.auth.config.GatewayInternalProperties;
import com.vcoding.common.auth.config.VcodingAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
        VcodingAuthProperties.class,
        GatewayInternalProperties.class,
        JwtService.class,
        GatewayUserHeaderService.class
})
public class VcodingGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(VcodingGatewayApplication.class, args);
    }
}
