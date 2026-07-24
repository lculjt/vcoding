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
    /** 指标快照主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属热点 ID，对应 gtm_trend_item.id。 */
    private Long trendItemId;
    /** 本次指标采集时间，同一批任务内相同热点只保留一条快照。 */
    private LocalDateTime capturedAt;
    /** 播放数或浏览数，主要用于 YouTube 等内容平台。 */
    private Long viewCount;
    /** 点赞数；平台不返回时保持为空，不用 0 伪造。 */
    private Long likeCount;
    /** 评论数；平台不返回时保持为空。 */
    private Long commentCount;
    /** 社区评分或平台分数，例如投票分；GitHub/YouTube 可为空。 */
    private BigDecimal score;
    /** GitHub Fork 数。 */
    private Long forkCount;
    /** GitHub Star 数。 */
    private Long starCount;
    /** 回复数或讨论数；当前一期来源通常为空。 */
    private Long replyCount;
    /** 已清洗的公开原始指标 JSON，不保存完整第三方响应。 */
    private String rawMetricsJson;
    /** 快照记录创建时间。 */
    private LocalDateTime createdAt;
}
