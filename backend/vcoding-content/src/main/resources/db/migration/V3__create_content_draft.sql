CREATE TABLE ct_content_draft
(
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '内容草稿主键',
    topic_id          BIGINT       NOT NULL COMMENT '所属选题 ID',
    user_id           BIGINT       NOT NULL COMMENT '创建人用户 ID，用于数据隔离',
    content_type      VARCHAR(32)  NOT NULL COMMENT '内容类型',
    title             VARCHAR(200) NOT NULL COMMENT '草稿标题',
    summary           VARCHAR(1000)         DEFAULT NULL COMMENT '摘要',
    body              MEDIUMTEXT            DEFAULT NULL COMMENT '正文内容',
    script_content    MEDIUMTEXT            DEFAULT NULL COMMENT '短视频脚本',
    cover_prompt      VARCHAR(500)          DEFAULT NULL COMMENT '封面图提示词',
    generation_source VARCHAR(32)  NOT NULL DEFAULT 'manual' COMMENT '生成来源：manual、ai',
    status            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0草稿，1已定稿',
    deleted_flag      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，1已删除',
    deleted_at        DATETIME              DEFAULT NULL COMMENT '逻辑删除时间',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_ct_content_draft_topic_deleted (topic_id, deleted_flag, updated_at),
    KEY idx_ct_content_draft_user_deleted (user_id, deleted_flag, updated_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'AI 内容平台内容草稿表';
