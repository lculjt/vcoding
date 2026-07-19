package com.vcoding.globaltrend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vcoding.global-trend.sources")
public class GlobalTrendSourceProperties {
    private Http http = new Http();
    private HackerNews hackerNews = new HackerNews();
    private GitHub github = new GitHub();
    private YouTube youtube = new YouTube();

    @Getter
    @Setter
    public static class Http {
        private int connectTimeoutMillis = 5000;
        private int readTimeoutMillis = 10000;
    }

    @Getter
    @Setter
    public static class HackerNews {
        private String baseUrl = "https://hacker-news.firebaseio.com/v0";
        private int sampleSize = 5;
    }

    @Getter
    @Setter
    public static class GitHub {
        private String baseUrl = "https://api.github.com";
        private String token;
        private String searchQuery = "stars:>1000";
        private int sampleSize = 5;
    }

    @Getter
    @Setter
    public static class YouTube {
        private String baseUrl = "https://www.googleapis.com/youtube/v3";
        private String apiKey;
        private String regionCode = "US";
        private String videoCategoryId;
        private int sampleSize = 5;
    }
}
