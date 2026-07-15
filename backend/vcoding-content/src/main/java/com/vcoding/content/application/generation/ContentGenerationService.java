package com.vcoding.content.application.generation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import com.vcoding.content.api.dto.ContentGenerationRequest;
import com.vcoding.content.api.dto.ContentGenerationResponse;
import com.vcoding.content.application.ai.ContentAiClient;
import com.vcoding.content.application.ai.ContentAiPromptBuilder;
import com.vcoding.content.application.draft.ContentDraftService;
import com.vcoding.content.application.topic.TopicService;
import com.vcoding.content.domain.generation.ContentGenerationResult;
import com.vcoding.content.domain.generation.GenerationRunStatus;
import com.vcoding.content.domain.generation.GenerationTaskType;
import com.vcoding.content.domain.topic.TopicStatus;
import com.vcoding.content.infrastructure.persistence.entity.AiGenerationRunEntity;
import com.vcoding.content.infrastructure.persistence.entity.TopicEntity;
import com.vcoding.content.infrastructure.persistence.mapper.AiGenerationRunMapper;
import com.vcoding.content.infrastructure.persistence.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentGenerationService {
    private final AiGenerationRunMapper aiGenerationRunMapper;
    private final TopicMapper topicMapper;
    private final TopicService topicService;
    private final ContentDraftService contentDraftService;
    private final ContentAiClient contentAiClient;
    private final ContentAiPromptBuilder contentAiPromptBuilder;

    /**
     * 触发 AI 生成任务。无论成功或失败都会落库运行记录，避免前端丢失失败上下文。
     */
    @Transactional(rollbackFor = Exception.class)
    public ContentGenerationResponse generate(Long userId, Long topicId, ContentGenerationRequest request) {
        GenerationTaskType taskType = parseTaskType(request.getTaskType());
        TopicEntity topic = topicService.requireOwnedTopicEntity(userId, topicId);

        markTopicGenerating(topic);

        AiGenerationRunEntity run = new AiGenerationRunEntity();
        run.setTopicId(topicId);
        run.setUserId(userId);
        run.setTaskType(taskType.getCode());
        run.setStatus(GenerationRunStatus.RUNNING.getCode());
        aiGenerationRunMapper.insert(run);

        long startedAt = System.currentTimeMillis();
        try {
            String prompt = contentAiPromptBuilder.build(topic, taskType);
            ContentGenerationResult result = contentAiClient.generate(topic, taskType, prompt);
            long durationMs = System.currentTimeMillis() - startedAt;

            applyResult(run, result);
            run.setStatus(GenerationRunStatus.SUCCESS.getCode());
            run.setModelName(contentAiClient.modelName());
            run.setDurationMs(durationMs);
            run.setErrorMessage(null);

            if (shouldCreateDraft(taskType)) {
                Long draftId = contentDraftService.createFromAiGeneration(userId, topic, result, taskType);
                run.setDraftId(draftId);
            }

            aiGenerationRunMapper.updateById(run);
            syncTopicStatusAfterSuccess(topic);
            return toResponse(run);
        } catch (Exception exception) {
            long durationMs = System.currentTimeMillis() - startedAt;
            run.setStatus(GenerationRunStatus.FAILED.getCode());
            run.setDurationMs(durationMs);
            run.setModelName(contentAiClient.modelName());
            run.setErrorMessage(truncateErrorMessage(exception));
            aiGenerationRunMapper.updateById(run);
            syncTopicStatusAfterFailure(topic);
            return toResponse(run);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ContentGenerationResponse retry(Long userId, Long runId) {
        AiGenerationRunEntity previousRun = requireOwnRun(userId, runId);
        if (GenerationRunStatus.FAILED.getCode() != previousRun.getStatus()) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "仅失败任务支持重试");
        }

        ContentGenerationRequest request = new ContentGenerationRequest();
        request.setTaskType(previousRun.getTaskType());
        return generate(userId, previousRun.getTopicId(), request);
    }

    public List<ContentGenerationResponse> listByTopic(Long userId, Long topicId) {
        topicService.requireOwnedTopicEntity(userId, topicId);

        return aiGenerationRunMapper.selectList(new LambdaQueryWrapper<AiGenerationRunEntity>()
                        .eq(AiGenerationRunEntity::getTopicId, topicId)
                        .eq(AiGenerationRunEntity::getUserId, userId)
                        .orderByDesc(AiGenerationRunEntity::getCreatedAt)
                        .orderByDesc(AiGenerationRunEntity::getId)
                        .last("LIMIT 20"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ContentGenerationResponse detail(Long userId, Long runId) {
        return toResponse(requireOwnRun(userId, runId));
    }

    private AiGenerationRunEntity requireOwnRun(Long userId, Long runId) {
        if (runId == null) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "运行记录 ID 不能为空");
        }

        AiGenerationRunEntity entity = aiGenerationRunMapper.selectOne(new LambdaQueryWrapper<AiGenerationRunEntity>()
                .eq(AiGenerationRunEntity::getId, runId)
                .eq(AiGenerationRunEntity::getUserId, userId)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 生成记录不存在或无权限访问");
        }
        return entity;
    }

    private GenerationTaskType parseTaskType(String taskType) {
        GenerationTaskType parsed = GenerationTaskType.fromCode(taskType);
        if (parsed == null) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "不支持的生成任务类型");
        }
        return parsed;
    }

    private boolean shouldCreateDraft(GenerationTaskType taskType) {
        return taskType == GenerationTaskType.ARTICLE || taskType == GenerationTaskType.VIDEO_SCRIPT;
    }

    private void markTopicGenerating(TopicEntity topic) {
        if (topic.getStatus() == TopicStatus.ARCHIVED.getCode()) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "已归档选题不能继续生成内容");
        }
        topic.setStatus(TopicStatus.GENERATING.getCode());
        topicMapper.updateById(topic);
    }

    private void syncTopicStatusAfterSuccess(TopicEntity topic) {
        TopicEntity latest = topicMapper.selectById(topic.getId());
        if (latest == null || Boolean.TRUE.equals(latest.getDeletedFlag())) {
            return;
        }
        latest.setStatus(TopicStatus.PENDING_GENERATE.getCode());
        topicMapper.updateById(latest);
    }

    private void syncTopicStatusAfterFailure(TopicEntity topic) {
        TopicEntity latest = topicMapper.selectById(topic.getId());
        if (latest == null || Boolean.TRUE.equals(latest.getDeletedFlag())) {
            return;
        }

        if (latest.getStatus() == TopicStatus.GENERATING.getCode()) {
            long draftCount = contentDraftService.countActiveDraftsForTopic(latest.getId());
            latest.setStatus(draftCount > 0
                    ? TopicStatus.PENDING_GENERATE.getCode()
                    : TopicStatus.DRAFT.getCode());
            topicMapper.updateById(latest);
        }
    }

    private void applyResult(AiGenerationRunEntity run, ContentGenerationResult result) {
        run.setResultTitle(trimToNull(result.title()));
        run.setResultSummary(trimToNull(result.summary()));
        run.setResultBody(trimToNull(result.body()));
        run.setResultScript(trimToNull(result.scriptContent()));
        run.setResultCoverPrompt(trimToNull(result.coverPrompt()));
        run.setResultTags(trimToNull(result.tags()));
        run.setResultOutline(trimToNull(result.outline()));
        run.setResultExtra(trimToNull(result.titleCandidates()));
    }

    private ContentGenerationResponse toResponse(AiGenerationRunEntity run) {
        GenerationRunStatus status = GenerationRunStatus.fromCode(run.getStatus());
        return new ContentGenerationResponse(
                run.getId(),
                run.getTopicId(),
                run.getDraftId(),
                run.getTaskType(),
                toStatusCode(status),
                run.getResultTitle(),
                run.getResultSummary(),
                run.getResultBody(),
                run.getResultScript(),
                run.getResultCoverPrompt(),
                run.getResultTags(),
                run.getResultOutline(),
                run.getResultExtra(),
                run.getErrorMessage(),
                run.getModelName(),
                run.getDurationMs(),
                run.getCreatedAt()
        );
    }

    private String toStatusCode(GenerationRunStatus status) {
        return switch (status) {
            case RUNNING -> "running";
            case SUCCESS -> "success";
            case FAILED -> "failed";
        };
    }

    private String truncateErrorMessage(Exception exception) {
        String message = exception instanceof BusinessException businessException
                ? businessException.getMessage()
                : exception.getMessage();
        if (!StringUtils.hasText(message)) {
            message = "AI 生成失败";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
