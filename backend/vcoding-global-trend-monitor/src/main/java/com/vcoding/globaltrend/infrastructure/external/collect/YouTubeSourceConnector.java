package com.vcoding.globaltrend.infrastructure.external.collect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vcoding.globaltrend.config.GlobalTrendSourceProperties;
import com.vcoding.globaltrend.domain.collect.SourceConnector;
import com.vcoding.globaltrend.domain.collect.TrendItemDraft;
import com.vcoding.globaltrend.infrastructure.external.validation.SourceHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class YouTubeSourceConnector implements SourceConnector {
    private static final String SOURCE_CODE = "youtube";

    private final GlobalTrendSourceProperties properties;
    private final SourceHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public String sourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public List<TrendItemDraft> collect() {
        GlobalTrendSourceProperties.YouTube youtube = properties.getYoutube();
        if (!StringUtils.hasText(youtube.getApiKey())) {
            throw new IllegalStateException("YouTube API Key 未配置");
        }
        // videos.list 的 chart=mostPopular 是一期获取 YouTube 热门视频的正式入口。
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(trimTrailingSlash(youtube.getBaseUrl()) + "/videos")
                .queryParam("part", "snippet,statistics,contentDetails")
                .queryParam("chart", "mostPopular")
                .queryParam("regionCode", youtube.getRegionCode())
                .queryParam("maxResults", normalizeSampleSize(youtube.getSampleSize()))
                .queryParam("key", youtube.getApiKey());
        // 分类 ID 可选；为空时读取地区综合热门，便于本地先跑通最小闭环。
        if (StringUtils.hasText(youtube.getVideoCategoryId())) {
            builder.queryParam("videoCategoryId", youtube.getVideoCategoryId());
        }
        SourceHttpClient.HttpResult response = httpClient.get(
                builder.build().encode().toUri(),
                headers -> headers.set("User-Agent", "vcoding-global-trend-monitor/0.1")
        );
        if (!response.is2xxSuccessful() || !StringUtils.hasText(response.body())) {
            if (response.statusCode() == 403) {
                throw new IllegalStateException("YouTube 请求被拒绝，请检查 API Key、配额或接口权限");
            }
            throw new IllegalStateException("YouTube videos.list 请求失败，HTTP " + response.statusCode());
        }

        try {
            JsonNode items = objectMapper.readTree(response.body()).path("items");
            if (!items.isArray()) {
                throw new IllegalStateException("YouTube 返回中缺少 items 数组");
            }
            List<TrendItemDraft> drafts = new ArrayList<>();
            for (JsonNode item : items) {
                // id 和标题是 YouTube 热点的最低可展示字段；缺失时跳过该条异常样本。
                String id = text(item, "id");
                JsonNode snippet = item.path("snippet");
                String title = text(snippet, "title");
                if (!StringUtils.hasText(id) || !StringUtils.hasText(title)) {
                    continue;
                }
                JsonNode statistics = item.path("statistics");
                drafts.add(new TrendItemDraft(
                        SOURCE_CODE,
                        id,
                        "https://www.youtube.com/watch?v=" + id,
                        title,
                        text(snippet, "channelTitle"),
                        "VIDEO",
                        "video",
                        youtube.getRegionCode(),
                        "en",
                        parseTime(text(snippet, "publishedAt")),
                        longValue(statistics, "viewCount"),
                        longValue(statistics, "likeCount"),
                        longValue(statistics, "commentCount"),
                        null,
                        null,
                        null,
                        null,
                        metricsJson(snippet, statistics)
                ));
            }
            return drafts;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("YouTube 返回内容不是有效 JSON", exception);
        }
    }

    private java.time.LocalDateTime parseTime(String value) {
        return StringUtils.hasText(value) ? OffsetDateTime.parse(value).toLocalDateTime() : null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Long longValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? null : value.asLong();
    }

    private String metricsJson(JsonNode snippet, JsonNode statistics) {
        // rawMetricsJson 只保留图表和排查需要的公开指标，不保存完整 videos.list 响应。
        ObjectNode metrics = objectMapper.createObjectNode();
        copyText(snippet, metrics, "categoryId");
        copyNumber(statistics, metrics, "viewCount");
        copyNumber(statistics, metrics, "likeCount");
        copyNumber(statistics, metrics, "commentCount");
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private void copyText(JsonNode source, ObjectNode target, String field) {
        JsonNode value = source.get(field);
        if (value != null && !value.isNull()) {
            target.set(field, value);
        }
    }

    private void copyNumber(JsonNode source, ObjectNode target, String field) {
        JsonNode value = source.get(field);
        if (value != null && value.isNumber()) {
            target.set(field, value);
        }
    }

    private int normalizeSampleSize(int sampleSize) {
        return Math.max(1, Math.min(sampleSize, 50));
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
