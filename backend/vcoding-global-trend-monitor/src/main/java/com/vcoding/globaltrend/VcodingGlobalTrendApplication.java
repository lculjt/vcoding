package com.vcoding.globaltrend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 海外热点聚合观察台后端入口。一期先提供系统骨架，采集和持久化能力按后续阶段逐步接入。
 */
@SpringBootApplication(scanBasePackages = "com.vcoding")
public class VcodingGlobalTrendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VcodingGlobalTrendApplication.class, args);
    }
}
