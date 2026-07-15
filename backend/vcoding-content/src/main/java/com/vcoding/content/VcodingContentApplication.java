package com.vcoding.content;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.vcoding.content.infrastructure.persistence.mapper")
@SpringBootApplication(scanBasePackages = "com.vcoding")
public class VcodingContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(VcodingContentApplication.class, args);
    }
}
