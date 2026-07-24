package com.vcoding.globaltrend.infrastructure.external.collect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vcoding.globaltrend.config.GlobalTrendSourceProperties;
import com.vcoding.globaltrend.domain.collect.SourceConnector;
import com.vcoding.globaltrend.domain.collect.TrendItemDraft;
import com.vcoding.globaltrend.infrastructure.external.validation.SourceHttpClient;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GitHubSourceConnector implements SourceConnector {
    private static final String SOURCE_CODE = "github";
    private static final Pattern STARS_TODAY_PATTERN = Pattern.compile("([\\d,]+)\\s+stars?\\s+today", Pattern.CASE_INSENSITIVE);
    private static final Set<String> SUPPORTED_SINCE = Set.of("daily", "weekly", "monthly");

    private final GlobalTrendSourceProperties properties;
    private final SourceHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public String sourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public List<TrendItemDraft> collect() {
        GlobalTrendSourceProperties.GitHub github = properties.getGithub();
        URI uri = buildTrendingUri(github);
        SourceHttpClient.HttpResult response = httpClient.get(uri, headers -> {
            headers.set("Accept", "text/html,application/xhtml+xml");
            headers.set("User-Agent", "vcoding-global-trend-monitor/0.1");
        });
        ensureSuccess(response);

        Document document = Jsoup.parse(response.body(), uri.toString());
        Elements repositories = document.select("article.Box-row");
        if (repositories.isEmpty()) {
            throw new IllegalStateException("GitHub Trending 页面中未找到仓库列表");
        }

        List<TrendItemDraft> drafts = new ArrayList<>();
        for (Element repository : repositories) {
            if (drafts.size() >= normalizeSampleSize(github.getSampleSize())) {
                break;
            }
            TrendItemDraft draft = toDraft(repository, github);
            if (draft != null) {
                drafts.add(draft);
            }
        }
        return drafts;
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

    private TrendItemDraft toDraft(Element repository, GlobalTrendSourceProperties.GitHub github) {
        Element titleLink = repository.selectFirst("h2 a[href]");
        if (titleLink == null) {
            return null;
        }
        String href = titleLink.attr("href").trim();
        String fullName = normalizeRepositoryName(href, titleLink.text());
        if (!StringUtils.hasText(fullName)) {
            return null;
        }

        String description = text(repository.selectFirst("p"));
        String programmingLanguage = text(repository.selectFirst("[itemprop=programmingLanguage]"));
        Long starCount = numberFromLink(repository, "/" + fullName + "/stargazers");
        Long forkCount = numberFromLink(repository, "/" + fullName + "/forks");
        Long starsToday = starsToday(repository);
        String canonicalUrl = "https://github.com/" + fullName;
        String owner = fullName.contains("/") ? fullName.substring(0, fullName.indexOf('/')) : null;

        return new TrendItemDraft(
                SOURCE_CODE,
                fullName.toLowerCase(Locale.ROOT),
                canonicalUrl,
                fullName,
                owner,
                "REPOSITORY",
                "developer-tools",
                null,
                StringUtils.hasText(github.getSpokenLanguageCode()) ? github.getSpokenLanguageCode().trim() : "en",
                LocalDateTime.now().withNano(0),
                null,
                null,
                null,
                starsToday == null ? null : BigDecimal.valueOf(starsToday),
                forkCount,
                starCount,
                null,
                metricsJson(description, programmingLanguage, starCount, forkCount, starsToday, normalizeSince(github.getSince()))
        );
    }

    private void ensureSuccess(SourceHttpClient.HttpResult response) {
        if (!response.is2xxSuccessful() || !StringUtils.hasText(response.body())) {
            if (response.statusCode() == 403 || response.statusCode() == 429) {
                throw new IllegalStateException("GitHub Trending 页面请求被拒绝或限流");
            }
            throw new IllegalStateException("GitHub Trending 页面请求失败，HTTP " + response.statusCode());
        }
    }

    private String normalizeRepositoryName(String href, String fallbackText) {
        if (StringUtils.hasText(href) && href.startsWith("/")) {
            String[] parts = href.substring(1).split("/");
            if (parts.length >= 2) {
                return parts[0] + "/" + parts[1];
            }
        }
        String compact = fallbackText == null ? "" : fallbackText.replaceAll("\\s+", "");
        return compact.contains("/") ? compact : null;
    }

    private Long numberFromLink(Element repository, String hrefSuffix) {
        Element link = repository.selectFirst("a[href='" + hrefSuffix + "']");
        if (link == null) {
            return null;
        }
        return parseLong(link.text());
    }

    private Long starsToday(Element repository) {
        Matcher matcher = STARS_TODAY_PATTERN.matcher(repository.text());
        return matcher.find() ? parseLong(matcher.group(1)) : null;
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        return StringUtils.hasText(digits) ? Long.parseLong(digits) : null;
    }

    private String text(Element element) {
        return element == null ? null : element.text().trim();
    }

    private String metricsJson(
            String description,
            String programmingLanguage,
            Long starCount,
            Long forkCount,
            Long starsToday,
            String since
    ) {
        // rawMetricsJson 只保留从 Trending 页面解析出的公开展示指标，不保存整页 HTML。
        ObjectNode metrics = objectMapper.createObjectNode();
        putText(metrics, "description", description);
        putText(metrics, "programming_language", programmingLanguage);
        putNumber(metrics, "stargazers_count", starCount);
        putNumber(metrics, "forks_count", forkCount);
        putNumber(metrics, "stars_today", starsToday);
        putText(metrics, "since", since);
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private void putText(ObjectNode target, String field, String value) {
        if (StringUtils.hasText(value)) {
            target.put(field, value);
        }
    }

    private void putNumber(ObjectNode target, String field, Long value) {
        if (value != null) {
            target.put(field, value);
        }
    }

    private int normalizeSampleSize(int sampleSize) {
        return Math.max(1, Math.min(sampleSize, 25));
    }

    private String normalizeSince(String since) {
        if (!StringUtils.hasText(since)) {
            return "daily";
        }
        String normalized = since.trim().toLowerCase(Locale.ROOT);
        return SUPPORTED_SINCE.contains(normalized) ? normalized : "daily";
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
