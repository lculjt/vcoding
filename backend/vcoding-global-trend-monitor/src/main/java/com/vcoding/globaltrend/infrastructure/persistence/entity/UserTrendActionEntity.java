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
    /** 用户操作记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 当前登录用户 ID，收藏、备注和归档必须按该字段隔离。 */
    private Long userId;
    /** 被操作的热点 ID。 */
    private Long trendItemId;
    /** 操作类型，例如 FAVORITE、IGNORE、ARCHIVE、NOTE、TAG。 */
    private String actionType;
    /** 用户备注内容。 */
    private String note;
    /** 用户自定义标签 JSON 数组。 */
    private String tagsJson;
    /** 操作记录创建时间。 */
    private LocalDateTime createdAt;
    /** 操作记录最近更新时间。 */
    private LocalDateTime updatedAt;
}
