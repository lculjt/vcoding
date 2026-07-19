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
@TableName("gtm_trend_item")
public class TrendItemEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sourceId;
    private String platformItemId;
    private String canonicalUrl;
    private String titleOriginal;
    private String titleZh;
    private String summaryZh;
    private String authorName;
    private String contentType;
    private String topicCode;
    private String keywordsJson;
    private String region;
    private String language;
    private LocalDateTime publishedAt;
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;
    private BigDecimal heatScore;
    private String scoreVersion;
    private LocalDateTime heatScoreUpdatedAt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
