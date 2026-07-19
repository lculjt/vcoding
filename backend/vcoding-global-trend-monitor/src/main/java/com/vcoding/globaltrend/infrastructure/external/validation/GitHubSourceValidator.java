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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GitHubSourceValidator implements SourceValidator {
    private static final String SOURCE_CODE = "github";
    private static final List<String> EXPECTED_FIELDS = List.of(
            "id", "full_name", "html_url", "description", "language",
            "stargazers_count", "forks_count", "created_at", "updated_at"
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
        GlobalTrendSourceProperties.GitHub github = properties.getGithub();
        URI uri = UriComponentsBuilder.fromUriString(trimTrailingSlash(github.getBaseUrl()) + "/search/repositories")
                .queryParam("q", github.getSearchQuery())
                .queryParam("sort", "updated")
                .queryParam("order", "desc")
                .queryParam("per_page", normalizeSampleSize(github.getSampleSize()))
                .build()
                .encode()
                .toUri();

        SourceHttpClient.HttpResult response = httpClient.get(uri, headers -> {
            headers.set("Accept", "application/vnd.github+json");
            headers.set("User-Agent", "vcoding-global-trend-monitor/0.1");
            if (StringUtils.hasText(github.getToken())) {
                headers.setBearerAuth(github.getToken());
            }
        });
        String authentication = StringUtils.hasText(github.getToken()) ? "BEARER_CONFIGURED" : "NONE";
        String rateLimit = rateLimitSummary(response);
        if (!response.is2xxSuccessful()) {
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    response.statusCode(),
                    elapsedMillis(startedAt),
                    authentication,
                    rateLimit,
                    response.statusCode() == 403 || response.statusCode() == 429
                            ? "GitHub 请求被限流或权限不足"
                            : "GitHub Search API 请求失败"
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
                        authentication,
                        rateLimit,
                        "GitHub 返回中缺少 items 数组"
                );
            }
            Set<String> observedFields = observedFields(items, EXPECTED_FIELDS);
            List<String> missingFields = EXPECTED_FIELDS.stream()
                    .filter(field -> !observedFields.contains(field))
                    .toList();
            return ValidationResultFactory.success(
                    SOURCE_CODE,
                    response.statusCode(),
                    items.size(),
                    elapsedMillis(startedAt),
                    authentication,
                    rateLimit,
                    new java.util.ArrayList<>(observedFields),
                    missingFields,
                    "GitHub Search API 验证成功，已读取仓库趋势字段"
            );
        } catch (JsonProcessingException exception) {
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    response.statusCode(),
                    elapsedMillis(startedAt),
                    authentication,
                    rateLimit,
                    "GitHub 返回内容不是有效 JSON"
            );
        }
    }

    private Set<String> observedFields(JsonNode items, List<String> expectedFields) {
        Set<String> observedFields = new LinkedHashSet<>();
        for (JsonNode item : items) {
            for (String field : expectedFields) {
                if (item.has(field) && !item.get(field).isNull()) {
                    observedFields.add(field);
                }
            }
        }
        return observedFields;
    }

    private String rateLimitSummary(SourceHttpClient.HttpResult response) {
        String remaining = response.headers().getFirst("X-RateLimit-Remaining");
        String limit = response.headers().getFirst("X-RateLimit-Limit");
        String reset = response.headers().getFirst("X-RateLimit-Reset");
        if (!StringUtils.hasText(remaining) && !StringUtils.hasText(limit)) {
            return "UNREPORTED";
        }
        return "remaining=" + remaining + ",limit=" + limit + ",resetEpoch=" + reset;
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
