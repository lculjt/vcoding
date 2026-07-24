package com.vcoding.globaltrend.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("gtm_collect_job")
public class CollectJobEntity {
    /** 采集任务主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 本次任务采集的数据源 ID。 */
    private Long sourceId;
    /** 触发方式，例如 MANUAL、SCHEDULED、RETRY。 */
    private String triggerType;
    /** 任务状态，例如 RUNNING、SUCCESS、PARTIAL_SUCCESS、FAILED。 */
    private String status;
    /** 任务开始时间。 */
    private LocalDateTime startedAt;
    /** 任务结束时间。 */
    private LocalDateTime finishedAt;
    /** connector 从第三方平台拉取到的原始条数。 */
    private Integer fetchedCount;
    /** 本次新增入库的热点数量。 */
    private Integer insertedCount;
    /** 本次更新已有热点的数量。 */
    private Integer updatedCount;
    /** 本次在内存去重阶段丢弃的重复数量。 */
    private Integer deduplicatedCount;
    /** 本次处理失败的条数或任务失败计数。 */
    private Integer failedCount;
    /** 脱敏后的失败摘要，不包含 API Key、Token 或完整第三方响应。 */
    private String errorSummary;
    /** 链路追踪 ID，后续接入统一日志链路时使用。 */
    private String traceId;
    /** 任务记录创建时间。 */
    private LocalDateTime createdAt;
    /** 任务记录最近更新时间。 */
    private LocalDateTime updatedAt;
}
