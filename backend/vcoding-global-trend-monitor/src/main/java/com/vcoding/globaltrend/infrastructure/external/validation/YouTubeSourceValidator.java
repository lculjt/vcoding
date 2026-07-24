package com.vcoding.globaltrend.infrastructure.external.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcoding.globaltrend.config.GlobalTrendSourceProperties;
import com.vcoding.globaltrend.domain.sourcevalidation.SourceValidationResult;
import com.vcoding.globaltrend.domain.sourcevalidation.SourceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class YouTubeSourceValidator implements SourceValidator {
    private static final String SOURCE_CODE = "youtube";
    private static final List<String> EXPECTED_FIELDS = List.of(
            "id", "snippet.title", "snippet.channelId", "snippet.publishedAt",
            "snippet.categoryId", "statistics.viewCount", "statistics.likeCount",
            "statistics.commentCount"
    );

    private final GlobalTrendSourceProperties properties;
    private final SourceHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public String sourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public SourceValidationResult validate() {
        Instant startedAt = Instant.now();
        GlobalTrendSourceProperties.YouTube youtube = properties.getYoutube();
        if (!StringUtils.hasText(youtube.getApiKey())) {
            // 未配置 Key 时直接返回可读失败结果，不发起外部请求，避免误导为网络问题。
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    0,
                    elapsedMillis(startedAt),
                    "API_KEY_MISSING",
                    "QUOTA_COST=1_PER_VIDEOS_LIST",
                    "未配置 YouTube API Key"
            );
        }

        // videos.list + chart=mostPopular 是一期验证 YouTube 热门榜字段的最小请求。
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(trimTrailingSlash(youtube.getBaseUrl()) + "/videos")
                .queryParam("part", "snippet,statistics,contentDetails")
                .queryParam("chart", "mostPopular")
                .queryParam("regionCode", youtube.getRegionCode())
                .queryParam("maxResults", normalizeSampleSize(youtube.getSampleSize()))
                .queryParam("key", youtube.getApiKey());
        // 分类 ID 可选；为空时验证地区综合热门。
        if (StringUtils.hasText(youtube.getVideoCategoryId())) {
            builder.queryParam("videoCategoryId", youtube.getVideoCategoryId());
        }

        SourceHttpClient.HttpResult response = httpClient.get(
                builder.build().encode().toUri(),
                headers -> headers.set("User-Agent", "vcoding-global-trend-monitor/0.1")
        );
        if (!response.is2xxSuccessful()) {
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    response.statusCode(),
                    elapsedMillis(startedAt),
                    "API_KEY_CONFIGURED",
                    "QUOTA_COST=1_PER_VIDEOS_LIST",
                    response.statusCode() == 403
                            ? "YouTube 请求被拒绝，请检查 API Key、配额或接口权限"
                            : "YouTube videos.list 请求失败"
            );
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode items = root.path("items");
            if (!items.isArray()) {
                return ValidationResultFactory.failure(
                        SOURCE_CODE,
                        response.statusCode(),
                        elapsedMillis(startedAt),
                        "API_KEY_CONFIGURED",
                        "QUOTA_COST=1_PER_VIDEOS_LIST",
                        "YouTube 返回中缺少 items 数组"
                );
            }
            // YouTube 字段有嵌套结构，这里只记录关键字段是否出现，不保存完整视频响应。
            Set<String> observedFields = observedFields(items, EXPECTED_FIELDS);
            List<String> missingFields = EXPECTED_FIELDS.stream()
                    .filter(field -> !observedFields.contains(field))
                    .toList();
            return ValidationResultFactory.success(
                    SOURCE_CODE,
                    response.statusCode(),
                    items.size(),
                    elapsedMillis(startedAt),
                    "API_KEY_CONFIGURED",
                    "QUOTA_COST=1_PER_VIDEOS_LIST",
                    new ArrayList<>(observedFields),
                    missingFields,
                    "YouTube videos.list 验证成功，已读取热门视频字段"
            );
        } catch (JsonProcessingException exception) {
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    response.statusCode(),
                    elapsedMillis(startedAt),
                    "API_KEY_CONFIGURED",
                    "QUOTA_COST=1_PER_VIDEOS_LIST",
                    "YouTube 返回内容不是有效 JSON"
            );
        }
    }

    private Set<String> observedFields(JsonNode items, List<String> expectedFields) {
        Set<String> observedFields = new LinkedHashSet<>();
        for (JsonNode item : items) {
            for (String field : expectedFields) {
                JsonNode value = item.at("/" + field.replace('.', '/'));
                if (!value.isMissingNode() && !value.isNull() && !value.isContainerNode()) {
                    observedFields.add(field);
                }
            }
        }
        return observedFields;
    }

    private int normalizeSampleSize(int sampleSize) {
        return Math.max(1, Math.min(sampleSize, 50));
    }

    private long elapsedMillis(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
