package com.vcoding.globaltrend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vcoding.global-trend.sources")
public class GlobalTrendSourceProperties {
    /** 第三方接口通用 HTTP 超时配置。 */
    private Http http = new Http();
    /** GitHub 数据源配置。 */
    private GitHub github = new GitHub();
    /** YouTube 数据源配置。 */
    private YouTube youtube = new YouTube();

    @Getter
    @Setter
    public static class Http {
        /** 建立连接的超时时间，单位毫秒。 */
        private int connectTimeoutMillis = 5000;
        /** 读取响应的超时时间，单位毫秒。 */
        private int readTimeoutMillis = 10000;
    }

    @Getter
    @Setter
    public static class GitHub {
        /** GitHub Trending 页面根地址。 */
        private String trendingBaseUrl = "https://github.com/trending";
        /** 编程语言筛选，对应 GitHub Trending URL 路径，例如 java、typescript；为空表示 Any。 */
        private String trendingLanguage;
        /** 自然语言筛选，对应 spoken_language_code 参数，例如 en、zh；为空表示 Any。 */
        private String spokenLanguageCode;
        /** 时间范围筛选，只允许 daily、weekly、monthly。 */
        private String since = "daily";
        /** 单次验证或采集的最大样本数，connector 会限制在页面返回数量范围内。 */
        private int sampleSize = 5;
    }

    @Getter
    @Setter
    public static class YouTube {
        /** YouTube Data API 根地址。 */
        private String baseUrl = "https://www.googleapis.com/youtube/v3";
        /** YouTube API Key；必须通过环境变量或密钥服务注入。 */
        private String apiKey;
        /** YouTube 热门榜地区编码，例如 US。 */
        private String regionCode = "US";
        /** YouTube 视频分类 ID；为空时读取该地区综合热门。 */
        private String videoCategoryId;
        /** 单次验证或采集的最大样本数，connector 会限制在 1 到 50。 */
        private int sampleSize = 5;
    }
}
