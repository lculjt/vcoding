package com.vcoding.globaltrend.domain.collect;

/**
 * 采集任务执行后的对外结果摘要。
 *
 * @param jobId 采集任务 ID
 * @param sourceCode 数据源编码
 * @param status 任务最终状态，例如 SUCCESS、PARTIAL_SUCCESS、FAILED
 * @param fetchedCount 第三方平台返回的原始条数
 * @param insertedCount 本次新增热点数量
 * @param updatedCount 本次更新已有热点数量
 * @param deduplicatedCount 本次内存去重丢弃数量
 * @param failedCount 本次失败条数或任务失败计数
 * @param errorSummary 脱敏后的错误摘要
 */
public record CollectJobResult(
        Long jobId,
        String sourceCode,
        String status,
        int fetchedCount,
        int insertedCount,
        int updatedCount,
        int deduplicatedCount,
        int failedCount,
        String errorSummary
) {
}
