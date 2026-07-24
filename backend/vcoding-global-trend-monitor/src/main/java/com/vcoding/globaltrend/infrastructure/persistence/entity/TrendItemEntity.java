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
    /** 热点内容主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属数据源 ID，对应 gtm_source.id。 */
    private Long sourceId;
    /** 平台内稳定内容 ID；没有官方 ID 时使用内容指纹。 */
    private String platformItemId;
    /** 规范化原文链接，用于跳转和辅助去重。 */
    private String canonicalUrl;
    /** 平台返回的原始标题。 */
    private String titleOriginal;
    /** 中文标题，后续由翻译或摘要链路补充。 */
    private String titleZh;
    /** 中文摘要，生成失败时允许为空。 */
    private String summaryZh;
    /** 作者、频道、仓库 owner 等发布主体。 */
    private String authorName;
    /** 统一内容类型，例如 VIDEO、REPOSITORY。 */
    private String contentType;
    /** 主题编码，例如 developer-tools、video，用于筛选和图表聚合。 */
    private String topicCode;
    /** 关键词 JSON 数组，后续由主题分类或摘要链路补充。 */
    private String keywordsJson;
    /** 来源地区，例如 YouTube regionCode。 */
    private String region;
    /** 内容语言，例如 en。 */
    private String language;
    /** 平台原始发布时间。 */
    private LocalDateTime publishedAt;
    /** 第一次被本系统采集到的时间。 */
    private LocalDateTime firstSeenAt;
    /** 最近一次被采集或更新指标的时间。 */
    private LocalDateTime lastSeenAt;
    /** 当前综合热度分，跨平台统一到 0 到 100 的可比较口径。 */
    private BigDecimal heatScore;
    /** 热度评分规则版本，便于后续调整公式后追溯。 */
    private String scoreVersion;
    /** 最近一次更新热度分的时间。 */
    private LocalDateTime heatScoreUpdatedAt;
    /** 展示状态，例如 ACTIVE、HIDDEN、EXPIRED。 */
    private String status;
    /** 热点记录创建时间。 */
    private LocalDateTime createdAt;
    /** 热点记录最近更新时间。 */
    private LocalDateTime updatedAt;
}
