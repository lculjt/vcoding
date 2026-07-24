package com.vcoding.globaltrend.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("gtm_trend_daily_aggregate")
public class TrendDailyAggregateEntity {
    /** 日聚合记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计日期，按 Asia/Shanghai 业务日期归档。 */
    private LocalDate statDate;
    /** 数据源 ID；0 表示全平台聚合。 */
    private Long sourceId;
    /** 主题编码；空字符串表示全部主题。 */
    private String topicCode;
    /** 内容类型；空字符串表示全部类型。 */
    private String contentType;
    /** 当前维度下的热点数量。 */
    private Integer itemCount;
    /** 当前维度下的平均综合热度分。 */
    private BigDecimal avgHeatScore;
    /** 当前维度下的最高综合热度分。 */
    private BigDecimal maxHeatScore;
    /** 与上一比较窗口相比的增长率；数据不足时可为空。 */
    private BigDecimal growthRate;
    /** 用于聚合榜单排序的综合分。 */
    private BigDecimal rankScore;
    /** 聚合记录创建时间。 */
    private LocalDateTime createdAt;
    /** 聚合记录最近更新时间。 */
    private LocalDateTime updatedAt;
}
