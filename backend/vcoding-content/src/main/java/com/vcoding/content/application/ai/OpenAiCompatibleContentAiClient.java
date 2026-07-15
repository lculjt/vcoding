package com.vcoding.content.application.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import com.vcoding.content.domain.generation.ContentGenerationResult;
import com.vcoding.content.domain.generation.GenerationTaskType;
import com.vcoding.content.infrastructure.persistence.entity.TopicEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容 Chat Completions 客户端。配置 api-key 后可接入真实模型。
 */
@Component
@ConditionalOnProperty(name = "vcoding.content.ai.mode", havingValue = "openai-compatible")
public class OpenAiCompatibleContentAiClient implements ContentAiClient {
    private final ContentAiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleContentAiClient(ContentAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ContentGenerationResult generate(TopicEntity topic, GenerationTaskType taskType, String prompt) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "未配置 AI API Key，请设置 vcoding.content.ai.api-key");
        }

        Map<String, Object> requestBody = buildRequestBody(prompt);

        try {
            String responseBody = restClient.post()
                    .uri(properties.getEndpointPath())
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            return parseResult(responseBody);
        } catch (RestClientResponseException exception) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "AI 服务调用失败：" + exception.getResponseBodyAsString());
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "AI 服务调用失败：" + exception.getMessage());
        }
    }

    @Override
    public String modelName() {
        return properties.getModel();
    }

    private ContentGenerationResult parseResult(String responseBody) throws Exception {
        if (!StringUtils.hasText(responseBody)) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "AI 服务返回为空");
        }

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode error = root.path("error");
        if (!error.isMissingNode() && !error.isNull()) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "AI 服务返回错误：" + error.path("message").asText(error.toString()));
        }

        String content = root.path("choices").path(0).path("message").path("content").asText();
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "AI 服务未返回可解析内容");
        }

        JsonNode json = objectMapper.readTree(extractJsonObject(content));
        return new ContentGenerationResult(
                text(json, "title"),
                text(json, "summary"),
                text(json, "body"),
                text(json, "scriptContent"),
                text(json, "coverPrompt"),
                text(json, "tags"),
                text(json, "outline"),
                text(json, "titleCandidates")
        );
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("temperature", properties.getTemperature());
        if (properties.getMaxTokens() != null && properties.getMaxTokens() > 0) {
            requestBody.put("max_tokens", properties.getMaxTokens());
        }
        if (properties.isJsonMode()) {
            requestBody.put("response_format", Map.of("type", "json_object"));
        }
        requestBody.put("messages", List.of(
                Map.of(
                        "role", "system",
                        "content", """
                                你是专业的中文内容创作助手。
                                必须只返回一个 JSON 对象，不要返回 Markdown 代码块、解释文字或额外前后缀。
                                JSON 字段值如果是列表，请优先使用换行分隔的字符串，便于系统保存到草稿。
                                """
                ),
                Map.of("role", "user", "content", prompt)
        ));
        return requestBody;
    }

    private String extractJsonObject(String content) {
        String normalized = content.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }

        int start = normalized.indexOf('{');
        int end = normalized.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return normalized.substring(start, end + 1);
        }

        throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "AI 服务返回内容不是 JSON 对象");
    }

    private String text(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            List<String> values = new ArrayList<>();
            node.forEach(item -> values.add(item.asText()));
            return String.join("\n", values);
        }
        if (node.isObject()) {
            return node.toString();
        }
        return node.asText();
    }
}
