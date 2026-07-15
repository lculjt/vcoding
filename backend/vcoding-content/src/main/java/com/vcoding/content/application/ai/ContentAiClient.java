package com.vcoding.content.application.ai;

import com.vcoding.content.domain.generation.ContentGenerationResult;
import com.vcoding.content.domain.generation.GenerationTaskType;
import com.vcoding.content.infrastructure.persistence.entity.TopicEntity;

/**
 * 内容 AI 客户端边界。后续迁移到 vcoding-ai 模块时，优先替换该接口的实现。
 */
public interface ContentAiClient {
    ContentGenerationResult generate(TopicEntity topic, GenerationTaskType taskType, String prompt);

    String modelName();
}
