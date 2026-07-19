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
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sourceId;
    private String triggerType;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer fetchedCount;
    private Integer insertedCount;
    private Integer updatedCount;
    private Integer deduplicatedCount;
    private Integer failedCount;
    private String errorSummary;
    private String traceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
