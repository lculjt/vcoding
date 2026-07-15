package com.vcoding.content.application.topic;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vcoding.common.exception.BusinessException;
import com.vcoding.common.response.ErrorCode;
import com.vcoding.common.response.PageResponse;
import com.vcoding.content.api.dto.CreateTopicRequest;
import com.vcoding.content.api.dto.TopicQueryRequest;
import com.vcoding.content.api.dto.TopicResponse;
import com.vcoding.content.api.dto.UpdateTopicRequest;
import com.vcoding.content.domain.topic.ContentType;
import com.vcoding.content.domain.topic.TargetPlatform;
import com.vcoding.content.domain.topic.TargetPlatformSupport;
import com.vcoding.content.domain.topic.ToneStyle;
import com.vcoding.content.domain.topic.TopicStatus;
import com.vcoding.content.infrastructure.persistence.entity.TopicEntity;
import com.vcoding.content.infrastructure.persistence.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicMapper topicMapper;

    /**
     * 创建选题时强制绑定当前登录用户，后续所有查询和修改都以 userId 做数据隔离。
     */
    @Transactional(rollbackFor = Exception.class)
    public TopicResponse create(Long userId, CreateTopicRequest request) {
        TopicEntity entity = new TopicEntity();
        entity.setUserId(userId);
        entity.setTitle(request.getTitle().trim());
        entity.setContentDirection(trimToNull(request.getContentDirection()));
        entity.setTargetAudience(trimToNull(request.getTargetAudience()));
        entity.setKeywords(trimToNull(request.getKeywords()));
        entity.setTargetPlatforms(toTargetPlatformsStorage(request.getTargetPlatforms()));
        entity.setContentType(toContentTypeCode(request.getContentType()));
        entity.setToneStyle(toToneStyleCode(request.getToneStyle()));
        entity.setExpectedWordCount(request.getExpectedWordCount());
        entity.setStatus(TopicStatus.DRAFT.getCode());
        entity.setRemark(trimToNull(request.getRemark()));
        entity.setDeletedFlag(false);

        topicMapper.insert(entity);
        return toResponse(entity);
    }

    /**
     * 分页查询当前用户选题。搜索只覆盖标题和关键词，避免第一阶段引入过宽的模糊查询。
     */
    public PageResponse<TopicResponse> page(Long userId, TopicQueryRequest request) {
        Page<TopicEntity> page = topicMapper.selectPage(
                Page.of(request.getPageNo(), request.getPageSize()),
                queryWrapper(userId, request)
        );
        List<TopicResponse> records = page.getRecords()
                .stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    public TopicResponse detail(Long userId, Long id) {
        return toResponse(requireOwnedTopicEntity(userId, id));
    }

    /**
     * 供草稿等其他应用服务复用的选题归属校验。
     */
    public TopicEntity requireOwnedTopicEntity(Long userId, Long topicId) {
        return requireOwnTopic(userId, topicId);
    }

    /**
     * 更新前先按 userId + id 查询，确保用户不能通过猜测 ID 修改别人的选题。
     */
    @Transactional(rollbackFor = Exception.class)
    public TopicResponse update(Long userId, Long id, UpdateTopicRequest request) {
        TopicEntity entity = requireOwnTopic(userId, id);
        entity.setTitle(request.getTitle().trim());
        entity.setContentDirection(trimToNull(request.getContentDirection()));
        entity.setTargetAudience(trimToNull(request.getTargetAudience()));
        entity.setKeywords(trimToNull(request.getKeywords()));
        entity.setTargetPlatforms(toTargetPlatformsStorage(request.getTargetPlatforms()));
        entity.setContentType(toContentTypeCode(request.getContentType()));
        entity.setToneStyle(toToneStyleCode(request.getToneStyle()));
        entity.setExpectedWordCount(request.getExpectedWordCount());
        if (request.getStatus() != null) {
            entity.setStatus(toTopicStatus(request.getStatus()).getCode());
        }
        entity.setRemark(trimToNull(request.getRemark()));

        topicMapper.updateById(entity);
        return toResponse(topicMapper.selectById(id));
    }

    /**
     * 删除前同样校验归属。选题是后续草稿、审核和发布任务的源头，因此从第一阶段开始使用逻辑删除。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        TopicEntity entity = requireOwnTopic(userId, id);
        entity.setDeletedFlag(true);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setStatus(TopicStatus.ARCHIVED.getCode());
        topicMapper.updateById(entity);
    }

    private LambdaQueryWrapper<TopicEntity> queryWrapper(Long userId, TopicQueryRequest request) {
        LambdaQueryWrapper<TopicEntity> wrapper = new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getUserId, userId)
                .eq(TopicEntity::getDeletedFlag, false)
                .orderByDesc(TopicEntity::getCreatedAt)
                .orderByDesc(TopicEntity::getId);

        String keyword = trimToNull(request.getKeyword());
        if (StringUtils.hasText(keyword)) {
            wrapper.and(query -> query
                    .like(TopicEntity::getTitle, keyword)
                    .or()
                    .like(TopicEntity::getKeywords, keyword));
        }
        if (request.getStatus() != null) {
            wrapper.eq(TopicEntity::getStatus, toTopicStatus(request.getStatus()).getCode());
        }
        if (StringUtils.hasText(request.getContentType())) {
            wrapper.eq(TopicEntity::getContentType, toContentTypeCode(request.getContentType()));
        }
        return wrapper;
    }

    private TopicEntity requireOwnTopic(Long userId, Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "选题 ID 不能为空");
        }

        TopicEntity entity = topicMapper.selectOne(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getId, id)
                .eq(TopicEntity::getUserId, userId)
                .eq(TopicEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "选题不存在或无权限访问");
        }
        return entity;
    }

    private TopicStatus toTopicStatus(Integer status) {
        if (status == null) {
            return TopicStatus.DRAFT;
        }
        return TopicStatus.fromCode(status);
    }

    private String toContentTypeCode(String code) {
        String value = trimToNull(code);
        ContentType contentType = ContentType.fromCode(value);
        if (contentType == null) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "不支持的内容类型");
        }
        return contentType.getCode();
    }

    private String toTargetPlatformsStorage(List<String> codes) {
        List<TargetPlatform> platforms = TargetPlatformSupport.parseRequestCodes(codes);
        return TargetPlatformSupport.toStorageValue(platforms);
    }

    private String toToneStyleCode(String code) {
        String value = trimToNull(code);
        if (!StringUtils.hasText(value)) {
            return ToneStyle.PROFESSIONAL.getCode();
        }

        ToneStyle toneStyle = ToneStyle.fromCode(value);
        if (toneStyle == null) {
            throw new BusinessException(ErrorCode.COMMON_BAD_REQUEST, "不支持的语气风格");
        }
        return toneStyle.getCode();
    }

    /**
     * 读取数据库中的内容类型时只返回受支持的编码，历史脏数据会被过滤为 null。
     */
    private String toContentTypeResponse(String code) {
        ContentType contentType = ContentType.fromCode(code);
        return contentType != null ? contentType.getCode() : null;
    }

    /**
     * 读取数据库中的语气风格时只返回受支持的编码，历史脏数据会被过滤为 null。
     */
    private String toToneStyleResponse(String code) {
        ToneStyle toneStyle = ToneStyle.fromCode(code);
        return toneStyle != null ? toneStyle.getCode() : null;
    }

    private TopicResponse toResponse(TopicEntity entity) {
        return new TopicResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getContentDirection(),
                entity.getTargetAudience(),
                entity.getKeywords(),
                TargetPlatformSupport.toCodeList(entity.getTargetPlatforms()),
                toContentTypeResponse(entity.getContentType()),
                toToneStyleResponse(entity.getToneStyle()),
                entity.getExpectedWordCount(),
                entity.getStatus(),
                entity.getRemark(),
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
}
