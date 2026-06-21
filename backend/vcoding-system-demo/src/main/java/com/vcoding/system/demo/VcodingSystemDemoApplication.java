package com.vcoding.system.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.vcoding")
public class VcodingSystemDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VcodingSystemDemoApplication.class, args);
    }
}
