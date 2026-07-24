package com.vcoding.globaltrend.infrastructure.external.validation;

import com.vcoding.globaltrend.config.GlobalTrendSourceProperties;
import com.vcoding.globaltrend.domain.sourcevalidation.SourceValidationResult;
import com.vcoding.globaltrend.domain.sourcevalidation.SourceValidator;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GitHubSourceValidator implements SourceValidator {
    private static final String SOURCE_CODE = "github";
    private static final Pattern STARS_TODAY_PATTERN = Pattern.compile("[\\d,]+\\s+stars?\\s+today", Pattern.CASE_INSENSITIVE);
    private static final Set<String> SUPPORTED_SINCE = Set.of("daily", "weekly", "monthly");
    private static final List<String> EXPECTED_FIELDS = List.of(
            "repository", "description", "programming_language", "stargazers_count", "forks_count", "stars_today"
    );

    private final GlobalTrendSourceProperties properties;
    private final SourceHttpClient httpClient;

    @Override
    public String sourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public SourceValidationResult validate() {
        Instant startedAt = Instant.now();
        GlobalTrendSourceProperties.GitHub github = properties.getGithub();
        // 验证阶段只读取 Trending 页面 HTML，不写入热点表。
        URI uri = buildTrendingUri(github);
        SourceHttpClient.HttpResult response = httpClient.get(uri, headers -> {
            headers.set("Accept", "text/html,application/xhtml+xml");
            headers.set("User-Agent", "vcoding-global-trend-monitor/0.1");
        });
        String rateLimit = "HTML_PAGE_NO_OFFICIAL_RATE_LIMIT_HEADER";
        if (!response.is2xxSuccessful()) {
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    response.statusCode(),
                    elapsedMillis(startedAt),
                    "NONE",
                    rateLimit,
                    response.statusCode() == 403 || response.statusCode() == 429
                            ? "GitHub Trending 页面请求被拒绝或限流"
                            : "GitHub Trending 页面请求失败"
            );
        }

        Document document = Jsoup.parse(response.body(), uri.toString());
        Elements repositories = document.select("article.Box-row");
        if (repositories.isEmpty()) {
            return ValidationResultFactory.failure(
                    SOURCE_CODE,
                    response.statusCode(),
                    elapsedMillis(startedAt),
                    "NONE",
                    rateLimit,
                    "GitHub Trending 页面中未找到仓库列表"
            );
        }

        // 只记录字段是否能从 HTML 中解析出来，不保存整页 HTML。
        Set<String> observedFields = observedFields(repositories);
        List<String> missingFields = EXPECTED_FIELDS.stream()
                .filter(field -> !observedFields.contains(field))
                .toList();
        return ValidationResultFactory.success(
                SOURCE_CODE,
                response.statusCode(),
                repositories.size(),
                elapsedMillis(startedAt),
                "NONE",
                rateLimit,
                new ArrayList<>(observedFields),
                missingFields,
                "GitHub Trending HTML 验证成功，已读取官网榜单字段"
        );
    }

    private URI buildTrendingUri(GlobalTrendSourceProperties.GitHub github) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(trimTrailingSlash(github.getTrendingBaseUrl()));
        if (StringUtils.hasText(github.getTrendingLanguage())) {
            builder.pathSegment(github.getTrendingLanguage().trim());
        }
        builder.queryParam("since", normalizeSince(github.getSince()));
        if (StringUtils.hasText(github.getSpokenLanguageCode())) {
            builder.queryParam("spoken_language_code", github.getSpokenLanguageCode().trim());
        }
        return builder.build().encode().toUri();
    }

    private Set<String> observedFields(Elements repositories) {
        Set<String> observedFields = new LinkedHashSet<>();
        for (Element repository : repositories) {
            if (repository.selectFirst("h2 a[href]") != null) {
                observedFields.add("repository");
            }
            if (StringUtils.hasText(text(repository.selectFirst("p")))) {
                observedFields.add("description");
            }
            if (StringUtils.hasText(text(repository.selectFirst("[itemprop=programmingLanguage]")))) {
                observedFields.add("programming_language");
            }
            if (repository.selectFirst("a[href$='/stargazers']") != null) {
                observedFields.add("stargazers_count");
            }
            if (repository.selectFirst("a[href$='/forks']") != null) {
                observedFields.add("forks_count");
            }
            if (STARS_TODAY_PATTERN.matcher(repository.text()).find()) {
                observedFields.add("stars_today");
            }
        }
        return observedFields;
    }

    private String text(Element element) {
        return element == null ? null : element.text().trim();
    }

    private String normalizeSince(String since) {
        if (!StringUtils.hasText(since)) {
            return "daily";
        }
        String normalized = since.trim().toLowerCase(Locale.ROOT);
        return SUPPORTED_SINCE.contains(normalized) ? normalized : "daily";
    }

    private long elapsedMillis(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
