package com.vcoding.content.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("ct_topic")
public class TopicEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String contentDirection;

    private String targetAudience;

    private String keywords;

    private String targetPlatforms;

    private String contentType;

    private String toneStyle;

    private Integer expectedWordCount;

    private Integer status;

    private String remark;

    private Boolean deletedFlag;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
