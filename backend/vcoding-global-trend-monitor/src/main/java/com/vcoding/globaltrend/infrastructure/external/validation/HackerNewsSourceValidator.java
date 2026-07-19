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

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HackerNewsSourceValidator implements SourceValidator {
    private static final String SOURCE_CODE = "hacker-news";
    private static final List<String> EXPECTED_FIELDS = List.of(
            "id", "type", "by", "time", "title", "url", "score", "descendants"
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
        SourceHttpClient.HttpResult topStories = httpClient.get(
                URI.create(trimTrailingSlash(properties.getHackerNews().getBaseUrl()) + "/topstories.json"),
                headers -> headers.set("User-Agent", "vcoding-global-trend-monitor/0.1")
        );
        if (!topStories.is2xxSuccessful()) {
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    topStories.statusCode(),
                    elapsedMillis(startedAt),
                    "NONE",
                    "UNREPORTED",
                    "Hacker News topstories 接口请求失败"
            );
        }

        try {
            JsonNode storyIds = objectMapper.readTree(topStories.body());
            if (!storyIds.isArray() || storyIds.isEmpty()) {
                return ValidationResultFactory.failure(
                        SOURCE_CODE,
                        topStories.statusCode(),
                        elapsedMillis(startedAt),
                        "NONE",
                        "UNREPORTED",
                        "Hacker News topstories 返回为空"
                );
            }

            int sampleSize = Math.min(normalizeSampleSize(properties.getHackerNews().getSampleSize()), storyIds.size());
            List<JsonNode> items = new ArrayList<>();
            for (int index = 0; index < sampleSize; index++) {
                long itemId = storyIds.get(index).asLong();
                SourceHttpClient.HttpResult itemResponse = httpClient.get(
                        URI.create(trimTrailingSlash(properties.getHackerNews().getBaseUrl()) + "/item/" + itemId + ".json"),
                        headers -> headers.set("User-Agent", "vcoding-global-trend-monitor/0.1")
                );
                if (!itemResponse.is2xxSuccessful() || !StringUtils.hasText(itemResponse.body())) {
                    continue;
                }
                JsonNode item = objectMapper.readTree(itemResponse.body());
                if (item.isObject()) {
                    items.add(item);
                }
            }

            Set<String> observedFields = observedFields(items, EXPECTED_FIELDS);
            List<String> missingFields = EXPECTED_FIELDS.stream()
                    .filter(field -> !observedFields.contains(field))
                    .toList();
            return ValidationResultFactory.success(
                    SOURCE_CODE,
                    topStories.statusCode(),
                    items.size(),
                    elapsedMillis(startedAt),
                    "NONE",
                    "UNREPORTED",
                    new ArrayList<>(observedFields),
                    missingFields,
                    "Hacker News 验证成功，已读取 topstories 和 item 字段"
            );
        } catch (JsonProcessingException exception) {
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    topStories.statusCode(),
                    elapsedMillis(startedAt),
                    "NONE",
                    "UNREPORTED",
                    "Hacker News 返回内容不是有效 JSON"
            );
        }
    }

    private Set<String> observedFields(List<JsonNode> items, List<String> expectedFields) {
        Set<String> observedFields = new LinkedHashSet<>();
        for (JsonNode item : items) {
            for (String field : expectedFields) {
                if (item.hasNonNull(field)) {
                    observedFields.add(field);
                }
            }
        }
        return observedFields;
    }

    private int normalizeSampleSize(int sampleSize) {
        return Math.max(1, Math.min(sampleSize, 20));
    }

    private long elapsedMillis(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
