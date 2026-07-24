package com.vcoding.globaltrend.application.collect;

import com.vcoding.globaltrend.domain.collect.TrendItemDraft;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 一期先按平台分别归一化公开指标，再合成为 0 到 100 的可比较分数。
 * 原始播放数、Star 数等仍单独保存，避免把不同平台的量纲直接相加。
 */
@Component
public class HeatScoreCalculator {
    public BigDecimal calculate(TrendItemDraft draft) {
        // 不同平台的原始指标量纲不同，必须先按平台自己的参考值归一化，再映射到统一热度分。
        double score = switch (draft.sourceCode()) {
            case "github" -> weighted(
                    // GitHub Trending HTML 中 score 表示 stars today，比总 Star 更能体现当日升温。
                    normalized(draft.score(), 5_000), 0.45,
                    normalized(draft.starCount(), 50_000), 0.40,
                    normalized(draft.forkCount(), 10_000), 0.15
            );
            case "youtube" -> weighted(
                    normalized(draft.viewCount(), 10_000_000), 0.65,
                    normalized(draft.likeCount(), 500_000), 0.20,
                    normalized(draft.commentCount(), 50_000), 0.15
            );
            default -> normalized(firstMetric(draft), 1_000);
        };
        return BigDecimal.valueOf(Math.max(0, Math.min(100, score)))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private double firstMetric(TrendItemDraft draft) {
        // 未显式配置评分规则的新平台，先取第一个可用规模指标作为兜底，避免返回完全不可排序的数据。
        if (draft.viewCount() != null) {
            return draft.viewCount();
        }
        if (draft.starCount() != null) {
            return draft.starCount();
        }
        if (draft.score() != null) {
            return draft.score().doubleValue();
        }
        return 0;
    }

    private double weighted(double... valuesAndWeights) {
        // 参数按 value、weight 交替传入，用来表达简单加权求和，调用处能直接看到每个指标权重。
        double result = 0;
        for (int index = 0; index < valuesAndWeights.length; index += 2) {
            result += valuesAndWeights[index] * valuesAndWeights[index + 1];
        }
        return result;
    }

    private double normalized(Number value, double reference) {
        if (value == null || value.doubleValue() <= 0) {
            return 0;
        }
        // log1p 可以压缩头部平台的大数值差异，避免极热视频或仓库把分数完全拉满。
        return Math.min(100, Math.log1p(value.doubleValue()) / Math.log1p(reference) * 100);
    }
}
