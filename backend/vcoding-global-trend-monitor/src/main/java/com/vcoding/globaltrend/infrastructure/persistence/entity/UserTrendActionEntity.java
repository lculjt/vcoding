package com.vcoding.globaltrend.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("gtm_user_trend_action")
public class UserTrendActionEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long trendItemId;
    private String actionType;
    private String note;
    private String tagsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
