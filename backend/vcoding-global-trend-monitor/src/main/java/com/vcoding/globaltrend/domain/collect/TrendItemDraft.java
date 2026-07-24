package com.vcoding.globaltrend.domain.collect;

import java.time.LocalDateTime;

/**
 * 数据源适配器输出的统一中间对象。该对象不直接暴露给前端，避免平台字段差异扩散到业务层。
 *
 * @param sourceCode 数据源编码，用于定位来源和匹配 gtm_source.code
 * @param platformItemId 平台内稳定内容 ID，没有官方 ID 时使用内容指纹
 * @param canonicalUrl 规范化原文链接，用于跳转和辅助去重
 * @param titleOriginal 平台返回的原始标题
 * @param authorName 作者、频道、仓库 owner 等发布主体
 * @param contentType 统一内容类型，例如 VIDEO、REPOSITORY
 * @param topicCode 主题编码，用于筛选和聚合
 * @param region 来源地区，不分地区的平台可为空
 * @param language 内容语言
 * @param publishedAt 平台原始发布时间
 * @param viewCount 播放数或浏览数
 * @param likeCount 点赞数
 * @param commentCount 评论数
 * @param score 社区评分或平台分数
 * @param forkCount GitHub Fork 数
 * @param starCount GitHub Star 数
 * @param replyCount 回复数或讨论数
 * @param rawMetricsJson 已清洗的公开原始指标 JSON，不保存完整第三方响应
 */
public record TrendItemDraft(
        String sourceCode,
        String platformItemId,
        String canonicalUrl,
        String titleOriginal,
        String authorName,
        String contentType,
        String topicCode,
        String region,
        String language,
        LocalDateTime publishedAt,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        java.math.BigDecimal score,
        Long forkCount,
        Long starCount,
        Long replyCount,
        String rawMetricsJson
) {
}
