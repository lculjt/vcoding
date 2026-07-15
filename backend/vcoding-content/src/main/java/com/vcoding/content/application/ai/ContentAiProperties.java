package com.vcoding.content.application.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内容系统 AI 调用配置。默认 mock 模式，便于本地联调；生产可切换 openai-compatible。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "vcoding.content.ai")
public class ContentAiProperties {
    /**
     * mock：本地模板生成；openai-compatible：调用 OpenAI 兼容 Chat Completions 接口。
     */
    private String mode = "mock";

    private String baseUrl = "https://api.openai.com/v1";

    private String apiKey = "";

    private String model = "gpt-4o-mini";

    /**
     * OpenAI 兼容接口路径。不同供应商通常 baseUrl 不同，但路径大多保持该默认值。
     */
    private String endpointPath = "/chat/completions";

    private int timeoutSeconds = 60;

    private double temperature = 0.7;

    /**
     * 开启 JSON 模式后，模型必须返回 JSON 对象；部分兼容供应商不支持时可关闭。
     */
    private boolean jsonMode = true;

    /**
     * 可选最大输出 token 数。不配置时交给模型服务默认值。
     */
    private Integer maxTokens;
}
