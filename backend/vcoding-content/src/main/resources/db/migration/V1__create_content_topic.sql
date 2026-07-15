CREATE TABLE ct_topic
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '选题主键',
    user_id             BIGINT       NOT NULL COMMENT '创建人用户 ID，用于用户数据隔离',
    title               VARCHAR(120) NOT NULL COMMENT '选题标题',
    content_direction   VARCHAR(120)          DEFAULT NULL COMMENT '内容方向',
    target_audience     VARCHAR(120)          DEFAULT NULL COMMENT '目标受众',
    keywords            VARCHAR(500)          DEFAULT NULL COMMENT '核心关键词，第一阶段使用逗号分隔',
    target_platforms    VARCHAR(255)          DEFAULT NULL COMMENT '目标平台，第一阶段使用逗号分隔',
    content_type        VARCHAR(32)  NOT NULL COMMENT '内容类型',
    tone_style          VARCHAR(32)           DEFAULT NULL COMMENT '语气风格',
    expected_word_count INT                   DEFAULT NULL COMMENT '期望字数',
    status              TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0草稿，1待生成，2生成中，3待审核，4已完成，5已归档',
    remark              VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_ct_topic_user_created (user_id, created_at),
    KEY idx_ct_topic_user_status (user_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'AI 内容平台选题表';
