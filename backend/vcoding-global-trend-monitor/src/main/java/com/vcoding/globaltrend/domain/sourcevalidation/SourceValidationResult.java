package com.vcoding.globaltrend.domain.sourcevalidation;

import java.time.Instant;
import java.util.List;

/**
 * 数据源小样本验证结果，不写入热点表，只用于确认第三方接口是否可用。
 *
 * @param sourceCode 数据源编码
 * @param success 是否验证成功
 * @param statusCode 第三方接口 HTTP 状态码，网络异常时可能为 0
 * @param itemCount 成功解析出的样本数量
 * @param durationMillis 本次验证耗时，单位毫秒
 * @param authentication 本次验证使用的认证方式，例如 NONE、API_KEY、TOKEN
 * @param rateLimit 第三方接口返回的限流摘要
 * @param observedFields 样本中已观测到的关键字段
 * @param missingFields 样本中缺失的关键字段
 * @param message 面向管理员的验证结论或失败原因
 * @param validatedAt 验证完成时间
 */
public record SourceValidationResult(
        String sourceCode,
        boolean success,
        int statusCode,
        int itemCount,
        long durationMillis,
        String authentication,
        String rateLimit,
        List<String> observedFields,
        List<String> missingFields,
        String message,
        Instant validatedAt
) {
}
