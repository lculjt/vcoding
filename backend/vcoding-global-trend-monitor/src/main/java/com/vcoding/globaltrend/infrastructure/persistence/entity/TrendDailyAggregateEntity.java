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
    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate statDate;
    private Long sourceId;
    private String topicCode;
    private String contentType;
    private Integer itemCount;
    private BigDecimal avgHeatScore;
    private BigDecimal maxHeatScore;
    private BigDecimal growthRate;
    private BigDecimal rankScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
