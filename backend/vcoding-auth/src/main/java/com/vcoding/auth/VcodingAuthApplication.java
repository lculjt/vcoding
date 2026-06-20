package com.vcoding.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.vcoding.auth.infrastructure.persistence.mapper")
@SpringBootApplication(scanBasePackages = "com.vcoding")
public class VcodingAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(VcodingAuthApplication.class, args);
    }
}
