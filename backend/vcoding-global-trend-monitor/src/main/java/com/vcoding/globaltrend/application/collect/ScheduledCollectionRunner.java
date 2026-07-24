package com.vcoding.globaltrend.application.collect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vcoding.globaltrend.infrastructure.persistence.entity.SourceEntity;
import com.vcoding.globaltrend.infrastructure.persistence.mapper.SourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每日定时采集入口。单个平台失败只结束该平台任务，不阻断同一批次的其他数据源。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "vcoding.global-trend.collect.schedule", name = "enabled", havingValue = "true")
public class ScheduledCollectionRunner {
    private final SourceMapper sourceMapper;
    private final GlobalTrendCollectService collectService;

    @Scheduled(
            cron = "${vcoding.global-trend.collect.schedule.cron:0 0 3 * * *}",
            zone = "${vcoding.global-trend.collect.schedule.zone:Asia/Shanghai}"
    )
    public void collectEnabledSources() {
        // 定时任务只扫描启用数据源；单个数据源失败会被捕获，不影响后续数据源继续执行。
        List<SourceEntity> sources = sourceMapper.selectList(new LambdaQueryWrapper<SourceEntity>()
                .eq(SourceEntity::getEnabled, true));
        for (SourceEntity source : sources) {
            try {
                collectService.collect(source.getCode(), "SCHEDULED");
            } catch (RuntimeException exception) {
                log.warn("定时采集未能启动: source={}", source.getCode(), exception);
            }
        }
    }
}
