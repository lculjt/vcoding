package com.vcoding.globaltrend.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("gtm_metric_snapshot")
public class MetricSnapshotEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long trendItemId;
    private LocalDateTime capturedAt;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private BigDecimal score;
    private Long forkCount;
    private Long starCount;
    private Long replyCount;
    private String rawMetricsJson;
    private LocalDateTime createdAt;
}
