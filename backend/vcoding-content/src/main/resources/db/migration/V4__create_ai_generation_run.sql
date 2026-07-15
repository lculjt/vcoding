CREATE TABLE ct_ai_generation_run
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'AI 生成运行记录主键',
    topic_id           BIGINT       NOT NULL COMMENT '所属选题 ID',
    user_id            BIGINT       NOT NULL COMMENT '触发用户 ID',
    task_type          VARCHAR(32)  NOT NULL COMMENT '任务类型：article、video-script、outline 等',
    status             TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0执行中，1成功，2失败',
    result_title       VARCHAR(200)          DEFAULT NULL COMMENT '生成标题',
    result_summary     VARCHAR(1000)         DEFAULT NULL COMMENT '生成摘要',
    result_body        MEDIUMTEXT            DEFAULT NULL COMMENT '生成正文',
    result_script      MEDIUMTEXT            DEFAULT NULL COMMENT '生成脚本',
    result_cover_prompt VARCHAR(500)         DEFAULT NULL COMMENT '生成封面提示词',
    result_tags        VARCHAR(500)          DEFAULT NULL COMMENT '生成标签，逗号分隔',
    result_outline     MEDIUMTEXT            DEFAULT NULL COMMENT '生成大纲',
    result_extra       VARCHAR(2000)         DEFAULT NULL COMMENT '额外结果，如标题候选',
    draft_id           BIGINT                DEFAULT NULL COMMENT '关联草稿 ID',
    error_message      VARCHAR(1000)         DEFAULT NULL COMMENT '失败原因',
    model_name         VARCHAR(64)           DEFAULT NULL COMMENT '模型名称',
    duration_ms        BIGINT                DEFAULT NULL COMMENT '执行耗时毫秒',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_ct_ai_generation_run_topic_created (topic_id, created_at),
    KEY idx_ct_ai_generation_run_user_created (user_id, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'AI 内容生成运行记录表';
