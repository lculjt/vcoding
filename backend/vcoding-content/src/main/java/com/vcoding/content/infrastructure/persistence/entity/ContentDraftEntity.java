package com.vcoding.content.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("ct_content_draft")
public class ContentDraftEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long topicId;

    private Long userId;

    private String contentType;

    private String title;

    private String summary;

    private String body;

    private String scriptContent;

    private String coverPrompt;

    private String generationSource;

    private Integer status;

    private Boolean deletedFlag;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
