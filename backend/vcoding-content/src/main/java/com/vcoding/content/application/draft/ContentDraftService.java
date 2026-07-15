package com.vcoding.content.application.draft;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import com.vcoding.content.api.dto.ContentDraftResponse;
import com.vcoding.content.api.dto.CreateContentDraftRequest;
import com.vcoding.content.api.dto.UpdateContentDraftRequest;
import com.vcoding.content.application.topic.TopicService;
import com.vcoding.content.domain.draft.DraftStatus;
import com.vcoding.content.domain.draft.GenerationSource;
import com.vcoding.content.domain.generation.ContentGenerationResult;
import com.vcoding.content.domain.generation.GenerationTaskType;
import com.vcoding.content.domain.topic.ContentType;
import com.vcoding.content.domain.topic.TopicStatus;
import com.vcoding.content.infrastructure.persistence.entity.ContentDraftEntity;
import com.vcoding.content.infrastructure.persistence.entity.TopicEntity;
import com.vcoding.content.infrastructure.persistence.mapper.ContentDraftMapper;
import com.vcoding.content.infrastructure.persistence.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentDraftService {
    private final ContentDraftMapper contentDraftMapper;
    private final TopicMapper topicMapper;
    private final TopicService topicService;

    /**
     * 创建草稿前先校验选题归属。首份草稿创建成功后，将选题从草稿推进到待生成。
     */
    @Transactional(rollbackFor = Exception.class)
    public ContentDraftResponse create(Long userId, Long topicId, CreateContentDraftRequest request) {
        TopicEntity topic = topicService.requireOwnedTopicEntity(userId, topicId);
        long existingDraftCount = countActiveDrafts(topicId);

        ContentDraftEntity entity = new ContentDraftEntity();
        entity.setTopicId(topicId);
        entity.setUserId(userId);
        entity.setTitle(request.getTitle().trim());
        entity.setContentType(resolveContentTypeCode(request.getContentType(), topic.getContentType()));
        entity.setSummary(trimToNull(request.getSummary()));
        entity.setBody(trimToNull(request.getBody()));
        entity.setScriptContent(trimToNull(request.getScriptContent()));
        entity.setCoverPrompt(trimToNull(request.getCoverPrompt()));
        entity.setGenerationSource(GenerationSource.MANUAL.getCode());
        entity.setStatus(DraftStatus.DRAFT.getCode());
        entity.setDeletedFlag(false);

        contentDraftMapper.insert(entity);
        syncTopicStatusAfterDraftCreated(topic, existingDraftCount);
        return toResponse(entity);
    }

    /**
     * 查询某个选题下的全部草稿，按最近更新时间倒序。
     */
    public List<ContentDraftResponse> listByTopic(Long userId, Long topicId) {
        topicService.requireOwnedTopicEntity(userId, topicId);

        List<ContentDraftEntity> entities = contentDraftMapper.selectList(new LambdaQueryWrapper<ContentDraftEntity>()
                .eq(ContentDraftEntity::getTopicId, topicId)
                .eq(ContentDraftEntity::getUserId, userId)
                .eq(ContentDraftEntity::getDeletedFlag, false)
                .orderByDesc(ContentDraftEntity::getUpdatedAt)
                .orderByDesc(ContentDraftEntity::getId));
        return entities.stream().map(this::toResponse).toList();
    }

    public ContentDraftResponse detail(Long userId, Long draftId) {
        return toResponse(requireOwnDraft(userId, draftId));
    }

    @Transactional(rollbackFor = Exception.class)
    public ContentDraftResponse update(Long userId, Long draftId, UpdateContentDraftRequest request) {
        ContentDraftEntity entity = requireOwnDraft(userId, draftId);
        entity.setTitle(request.getTitle().trim());
        entity.setContentType(toContentTypeCode(request.getContentType()));
        entity.setSummary(trimToNull(request.getSummary()));
        entity.setBody(trimToNull(request.getBody()));
        entity.setScriptContent(trimToNull(request.getScriptContent()));
        entity.setCoverPrompt(trimToNull(request.getCoverPrompt()));
        if (request.getStatus() != null) {
            entity.setStatus(toDraftStatus(request.getStatus()).getCode());
        }

        contentDraftMapper.updateById(entity);
        return toResponse(contentDraftMapper.selectById(draftId));
    }

    /**
     * 逻辑删除草稿。若删除后该选题已无草稿且处于待生成，则回退为草稿状态。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long draftId) {
        ContentDraftEntity entity = requireOwnDraft(userId, draftId);
        entity.setDeletedFlag(true);
        entity.setDeletedAt(LocalDateTime.now());
        contentDraftMapper.updateById(entity);
        syncTopicStatusAfterDraftDeleted(entity.getTopicId());
    }

    /**
     * 将 AI 生成结果保存为草稿，供后续平台适配使用。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createFromAiGeneration(
            Long userId,
            TopicEntity topic,
            ContentGenerationResult result,
            GenerationTaskType taskType
    ) {
        long existingDraftCount = countActiveDrafts(topic.getId());

        ContentDraftEntity entity = new ContentDraftEntity();
        entity.setTopicId(topic.getId());
        entity.setUserId(userId);
        entity.setTitle(resolveAiDraftTitle(result, topic));
        entity.setContentType(resolveAiContentType(topic, taskType));
        entity.setSummary(trimToNull(result.summary()));
        entity.setBody(trimToNull(result.body()));
        entity.setScriptContent(trimToNull(result.scriptContent()));
        entity.setCoverPrompt(trimToNull(result.coverPrompt()));
        entity.setGenerationSource(GenerationSource.AI.getCode());
        entity.setStatus(DraftStatus.DRAFT.getCode());
        entity.setDeletedFlag(false);

        contentDraftMapper.insert(entity);
        syncTopicStatusAfterDraftCreated(topic, existingDraftCount);
        return entity.getId();
    }

    private long countActiveDrafts(Long topicId) {
        return contentDraftMapper.selectCount(new LambdaQueryWrapper<ContentDraftEntity>()
                .eq(ContentDraftEntity::getTopicId, topicId)
                .eq(ContentDraftEntity::getDeletedFlag, false));
    }

    /**
     * 供生成服务判断选题是否已有草稿，从而决定失败后回退的状态。
     */
    public long countActiveDraftsForTopic(Long topicId) {
        return countActiveDrafts(topicId);
    }

    private void syncTopicStatusAfterDraftCreated(TopicEntity topic, long existingDraftCount) {
        if (existingDraftCount > 0) {
            return;
        }

        if (TopicStatus.DRAFT.getCode() == topic.getStatus()) {
            topic.setStatus(TopicStatus.PENDING_GENERATE.getCode());
            topicMapper.updateById(topic);
        }
    }

    private void syncTopicStatusAfterDraftDeleted(Long topicId) {
        TopicEntity topic = topicMapper.selectById(topicId);
        if (topic == null || Boolean.TRUE.equals(topic.getDeletedFlag())) {
            return;
        }

        if (countActiveDrafts(topicId) == 0 && TopicStatus.PENDING_GENERATE.getCode() == topic.getStatus()) {
            topic.setStatus(TopicStatus.DRAFT.getCode());
            topicMapper.updateById(topic);
        }
    }

    private ContentDraftEntity requireOwnDraft(Long userId, Long draftId) {
        if (draftId == null) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "草稿 ID 不能为空");
        }

        ContentDraftEntity entity = contentDraftMapper.selectOne(new LambdaQueryWrapper<ContentDraftEntity>()
                .eq(ContentDraftEntity::getId, draftId)
                .eq(ContentDraftEntity::getUserId, userId)
                .eq(ContentDraftEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "内容草稿不存在或无权限访问");
        }
        return entity;
    }

    private String resolveContentTypeCode(String requestCode, String topicCode) {
        String value = trimToNull(requestCode);
        if (!StringUtils.hasText(value)) {
            value = trimToNull(topicCode);
        }
        return toContentTypeCode(value);
    }

    private String toContentTypeCode(String code) {
        String value = trimToNull(code);
        ContentType contentType = ContentType.fromCode(value);
        if (contentType == null) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "不支持的内容类型");
        }
        return contentType.getCode();
    }

    private DraftStatus toDraftStatus(Integer status) {
        return DraftStatus.fromCode(status);
    }

    private ContentDraftResponse toResponse(ContentDraftEntity entity) {
        GenerationSource generationSource = GenerationSource.fromCode(entity.getGenerationSource());
        ContentType contentType = ContentType.fromCode(entity.getContentType());

        return new ContentDraftResponse(
                entity.getId(),
                entity.getTopicId(),
                entity.getUserId(),
                contentType != null ? contentType.getCode() : null,
                entity.getTitle(),
                entity.getSummary(),
                entity.getBody(),
                entity.getScriptContent(),
                entity.getCoverPrompt(),
                generationSource != null ? generationSource.getCode() : GenerationSource.MANUAL.getCode(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveAiDraftTitle(ContentGenerationResult result, TopicEntity topic) {
        if (StringUtils.hasText(result.title())) {
            return result.title().trim();
        }
        return topic.getTitle();
    }

    private String resolveAiContentType(TopicEntity topic, GenerationTaskType taskType) {
        if (taskType == GenerationTaskType.VIDEO_SCRIPT) {
            return ContentType.VIDEO_SCRIPT.getCode();
        }
        return toContentTypeCode(topic.getContentType());
    }
}
