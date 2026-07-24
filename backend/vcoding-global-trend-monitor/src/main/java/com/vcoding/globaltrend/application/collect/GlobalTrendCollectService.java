package com.vcoding.globaltrend.application.collect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import com.vcoding.globaltrend.domain.collect.CollectJobResult;
import com.vcoding.globaltrend.domain.collect.SourceConnector;
import com.vcoding.globaltrend.domain.collect.TrendItemDraft;
import com.vcoding.globaltrend.infrastructure.persistence.entity.CollectJobEntity;
import com.vcoding.globaltrend.infrastructure.persistence.entity.MetricSnapshotEntity;
import com.vcoding.globaltrend.infrastructure.persistence.entity.SourceEntity;
import com.vcoding.globaltrend.infrastructure.persistence.entity.TrendItemEntity;
import com.vcoding.globaltrend.infrastructure.persistence.mapper.CollectJobMapper;
import com.vcoding.globaltrend.infrastructure.persistence.mapper.MetricSnapshotMapper;
import com.vcoding.globaltrend.infrastructure.persistence.mapper.SourceMapper;
import com.vcoding.globaltrend.infrastructure.persistence.mapper.TrendItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GlobalTrendCollectService {
    /** 数据源表访问对象，用于读取启用状态和回写最近成功/失败时间。 */
    private final SourceMapper sourceMapper;
    /** 热点内容表访问对象，用于按平台内容 ID 幂等新增或更新。 */
    private final TrendItemMapper trendItemMapper;
    /** 指标快照表访问对象，用于保存每次采集时的原始公开指标。 */
    private final MetricSnapshotMapper metricSnapshotMapper;
    /** 采集任务表访问对象，用于记录任务状态、数量和失败摘要。 */
    private final CollectJobMapper collectJobMapper;
    /** 综合热度分计算器，把不同平台指标归一化到 0 到 100。 */
    private final HeatScoreCalculator heatScoreCalculator;
    /** 按 sourceCode 索引的采集器集合，例如 youtube -> YouTubeSourceConnector。 */
    private final Map<String, SourceConnector> connectors;
    /** 本地进程内采集锁，避免手动触发和定时触发在同一实例内并发写入。 */
    private final ReentrantLock collectLock = new ReentrantLock();

    public GlobalTrendCollectService(
            SourceMapper sourceMapper,
            TrendItemMapper trendItemMapper,
            MetricSnapshotMapper metricSnapshotMapper,
            CollectJobMapper collectJobMapper,
            HeatScoreCalculator heatScoreCalculator,
            List<SourceConnector> connectorList
    ) {
        this.sourceMapper = sourceMapper;
        this.trendItemMapper = trendItemMapper;
        this.metricSnapshotMapper = metricSnapshotMapper;
        this.collectJobMapper = collectJobMapper;
        this.heatScoreCalculator = heatScoreCalculator;
        // Spring 会注入所有 SourceConnector 实现，这里转成 Map，后续按数据源编码直接定位采集器。
        this.connectors = connectorList.stream().collect(Collectors.toUnmodifiableMap(
                SourceConnector::sourceCode,
                Function.identity()
        ));
    }

    public CollectJobResult collect(String sourceCode, String triggerType) {
        // 一期先用单实例本地锁防止重复采集；多实例部署后再升级为 Redis 锁或数据库运行约束。
        if (!collectLock.tryLock()) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "已有采集任务正在运行，请稍后重试");
        }
        try {
            return doCollect(sourceCode, triggerType);
        } finally {
            collectLock.unlock();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected CollectJobResult doCollect(String sourceCode, String triggerType) {
        // 先读取数据源配置并校验启用状态，避免停用来源仍被手动或定时任务触发。
        SourceEntity source = sourceMapper.selectOne(new LambdaQueryWrapper<SourceEntity>()
                .eq(SourceEntity::getCode, sourceCode)
                .last("LIMIT 1"));
        if (source == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "数据源不存在: " + sourceCode);
        }
        if (!Boolean.TRUE.equals(source.getEnabled())) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "数据源已停用: " + sourceCode);
        }

        CollectJobEntity job = createRunningJob(source, triggerType);
        SourceConnector connector = connectors.get(sourceCode);
        if (connector == null) {
            return failJob(source, job, "未找到数据源 connector: " + sourceCode);
        }

        try {
            // connector 只负责访问第三方平台并标准化字段；写库、评分和任务统计统一留在应用层。
            List<TrendItemDraft> drafts = connector.collect();
            job.setFetchedCount(drafts.size());

            // 同一批返回中可能出现重复内容，先在内存按平台 ID 或规范化 URL 去重，减少数据库写入压力。
            Map<String, TrendItemDraft> uniqueDrafts = new LinkedHashMap<>();
            int deduplicatedCount = 0;
            for (TrendItemDraft draft : drafts) {
                String key = dedupeKey(draft);
                if (uniqueDrafts.putIfAbsent(key, draft) != null) {
                    deduplicatedCount++;
                }
            }
            job.setDeduplicatedCount(deduplicatedCount);

            int insertedCount = 0;
            int updatedCount = 0;
            int failedCount = 0;
            List<String> errors = new ArrayList<>();
            // 同一轮采集使用同一个 capturedAt，便于后续按批次查看指标快照。
            LocalDateTime capturedAt = LocalDateTime.now().withNano(0);
            for (TrendItemDraft draft : uniqueDrafts.values()) {
                try {
                    PersistResult persistResult = persistDraft(source, draft, capturedAt);
                    if (persistResult.inserted()) {
                        insertedCount++;
                    } else {
                        updatedCount++;
                    }
                } catch (RuntimeException exception) {
                    // 单条写入失败不让整个平台任务直接失败，最多保留前三条脱敏摘要便于排查。
                    failedCount++;
                    if (errors.size() < 3) {
                        errors.add(sanitizeError(exception));
                    }
                    log.warn("热点写入失败: source={}, platformItemId={}", sourceCode, draft.platformItemId(), exception);
                }
            }
            job.setInsertedCount(insertedCount);
            job.setUpdatedCount(updatedCount);
            job.setFailedCount(failedCount);
            job.setStatus(failedCount == 0 ? "SUCCESS" : "PARTIAL_SUCCESS");
            job.setErrorSummary(joinErrors(errors));
            finishJob(job);
            // 即使有部分条目失败，只要平台请求和主流程跑通，仍记录最近成功时间。
            updateSourceAfterSuccess(source, failedCount > 0);
            return toResult(job, sourceCode);
        } catch (RuntimeException exception) {
            return failJob(source, job, sanitizeError(exception));
        }
    }

    private CollectJobEntity createRunningJob(SourceEntity source, String triggerType) {
        // 任务先落库为 RUNNING，保证后续平台请求失败时也能留下可追溯记录。
        LocalDateTime now = LocalDateTime.now();
        CollectJobEntity job = new CollectJobEntity();
        job.setSourceId(source.getId());
        job.setTriggerType(triggerType);
        job.setStatus("RUNNING");
        job.setStartedAt(now);
        job.setFetchedCount(0);
        job.setInsertedCount(0);
        job.setUpdatedCount(0);
        job.setDeduplicatedCount(0);
        job.setFailedCount(0);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        collectJobMapper.insert(job);
        return job;
    }

    private PersistResult persistDraft(SourceEntity source, TrendItemDraft draft, LocalDateTime capturedAt) {
        // 平台内容 ID 是当前一期的强去重键：再次采到同一内容时更新指标和 lastSeenAt。
        TrendItemEntity item = trendItemMapper.selectOne(new LambdaQueryWrapper<TrendItemEntity>()
                .eq(TrendItemEntity::getSourceId, source.getId())
                .eq(TrendItemEntity::getPlatformItemId, draft.platformItemId())
                .last("LIMIT 1"));
        boolean inserted = item == null;
        LocalDateTime now = LocalDateTime.now();
        if (inserted) {
            item = new TrendItemEntity();
            item.setSourceId(source.getId());
            item.setPlatformItemId(draft.platformItemId());
            item.setFirstSeenAt(now);
            item.setCreatedAt(now);
        }

        // 每次采集都刷新热点主体信息和热度分，让列表展示尽量接近第三方平台最新状态。
        item.setCanonicalUrl(draft.canonicalUrl());
        item.setTitleOriginal(draft.titleOriginal());
        item.setAuthorName(draft.authorName());
        item.setContentType(draft.contentType());
        item.setTopicCode(draft.topicCode());
        item.setRegion(draft.region());
        item.setLanguage(draft.language());
        item.setPublishedAt(draft.publishedAt());
        item.setLastSeenAt(now);
        item.setHeatScore(heatScoreCalculator.calculate(draft));
        item.setScoreVersion("v1");
        item.setHeatScoreUpdatedAt(now);
        item.setStatus("ACTIVE");
        item.setUpdatedAt(now);
        if (inserted) {
            trendItemMapper.insert(item);
        } else {
            trendItemMapper.updateById(item);
        }

        // 快照保存原始公开指标；同一热点同一批次只插入一次，避免重复触发产生相同时间点噪音。
        MetricSnapshotEntity snapshot = metricSnapshotMapper.selectOne(new LambdaQueryWrapper<MetricSnapshotEntity>()
                .eq(MetricSnapshotEntity::getTrendItemId, item.getId())
                .eq(MetricSnapshotEntity::getCapturedAt, capturedAt)
                .last("LIMIT 1"));
        if (snapshot == null) {
            snapshot = new MetricSnapshotEntity();
            snapshot.setTrendItemId(item.getId());
            snapshot.setCapturedAt(capturedAt);
            snapshot.setViewCount(draft.viewCount());
            snapshot.setLikeCount(draft.likeCount());
            snapshot.setCommentCount(draft.commentCount());
            snapshot.setScore(draft.score());
            snapshot.setForkCount(draft.forkCount());
            snapshot.setStarCount(draft.starCount());
            snapshot.setReplyCount(draft.replyCount());
            snapshot.setRawMetricsJson(draft.rawMetricsJson());
            snapshot.setCreatedAt(now);
            metricSnapshotMapper.insert(snapshot);
        }
        return new PersistResult(inserted);
    }

    private CollectJobResult failJob(SourceEntity source, CollectJobEntity job, String error) {
        // 平台级异常会把任务标记为 FAILED，并回写数据源最近失败时间，供管理页排查。
        job.setStatus("FAILED");
        job.setFailedCount(Math.max(1, job.getFailedCount()));
        job.setErrorSummary(error);
        finishJob(job);
        source.setLastFailureAt(LocalDateTime.now());
        source.setUpdatedAt(LocalDateTime.now());
        sourceMapper.updateById(source);
        return toResult(job, source.getCode());
    }

    private void finishJob(CollectJobEntity job) {
        job.setFinishedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        collectJobMapper.updateById(job);
    }

    private void updateSourceAfterSuccess(SourceEntity source, boolean partial) {
        LocalDateTime now = LocalDateTime.now();
        source.setLastSuccessAt(now);
        if (partial) {
            source.setLastFailureAt(now);
        }
        source.setUpdatedAt(now);
        sourceMapper.updateById(source);
    }

    private CollectJobResult toResult(CollectJobEntity job, String sourceCode) {
        return new CollectJobResult(
                job.getId(), sourceCode, job.getStatus(),
                defaultInt(job.getFetchedCount()), defaultInt(job.getInsertedCount()),
                defaultInt(job.getUpdatedCount()), defaultInt(job.getDeduplicatedCount()),
                defaultInt(job.getFailedCount()), job.getErrorSummary()
        );
    }

    private String dedupeKey(TrendItemDraft draft) {
        if (StringUtils.hasText(draft.platformItemId())) {
            return "id:" + draft.platformItemId();
        }
        return "url:" + draft.canonicalUrl();
    }

    private String sanitizeError(RuntimeException exception) {
        String message = exception.getMessage();
        if (!StringUtils.hasText(message)) {
            return exception.getClass().getSimpleName();
        }
        String sanitized = message.replaceAll("([?&]key=)[^&\\s]+", "$1***");
        return sanitized.substring(0, Math.min(sanitized.length(), 1900));
    }

    private String joinErrors(List<String> errors) {
        return errors.isEmpty() ? null : String.join("; ", errors);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 单条热点持久化结果。
     *
     * @param inserted true 表示新插入热点，false 表示更新已有热点
     */
    private record PersistResult(boolean inserted) {
    }
}
