package com.vcoding.content.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("ct_ai_generation_run")
public class AiGenerationRunEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long topicId;

    private Long userId;

    private String taskType;

    private Integer status;

    private String resultTitle;

    private String resultSummary;

    private String resultBody;

    private String resultScript;

    private String resultCoverPrompt;

    private String resultTags;

    private String resultOutline;

    private String resultExtra;

    private Long draftId;

    private String errorMessage;

    private String modelName;

    private Long durationMs;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
