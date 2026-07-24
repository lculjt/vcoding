package com.vcoding.globaltrend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vcoding.global-trend.collect.schedule")
public class GlobalTrendCollectProperties {
    /** 是否开启每日定时采集；本地默认关闭，避免调试时自动消耗第三方配额。 */
    private boolean enabled;
    /** 定时采集 cron 表达式，默认每天 03:00 执行。 */
    private String cron = "0 0 3 * * *";
    /** 定时任务使用的时区。 */
    private String zone = "Asia/Shanghai";
}
