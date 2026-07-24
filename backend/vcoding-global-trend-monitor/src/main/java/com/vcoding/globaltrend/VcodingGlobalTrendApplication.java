package com.vcoding.globaltrend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.vcoding.globaltrend.config.GlobalTrendSourceProperties;
import com.vcoding.globaltrend.config.GlobalTrendCollectProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 海外热点聚合观察台后端入口。一期先提供系统骨架，采集和持久化能力按后续阶段逐步接入。
 */
@SpringBootApplication(scanBasePackages = "com.vcoding")
@MapperScan("com.vcoding.globaltrend.infrastructure.persistence.mapper")
@EnableScheduling
@EnableConfigurationProperties({GlobalTrendSourceProperties.class, GlobalTrendCollectProperties.class})
public class VcodingGlobalTrendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VcodingGlobalTrendApplication.class, args);
    }
}
