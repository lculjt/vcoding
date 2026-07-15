ALTER TABLE ct_topic
    ADD COLUMN deleted_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，1已删除' AFTER remark,
    ADD COLUMN deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间' AFTER deleted_flag,
    ADD KEY idx_ct_topic_user_deleted_created (user_id, deleted_flag, created_at);
