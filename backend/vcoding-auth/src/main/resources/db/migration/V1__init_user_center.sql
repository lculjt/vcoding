CREATE TABLE uc_user
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    username      VARCHAR(32)  NOT NULL COMMENT '登录用户名',
    phone         VARCHAR(20)  NOT NULL COMMENT '手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '用户状态：1启用，0禁用',
    admin_flag    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否管理员',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_uc_user_username (username),
    UNIQUE KEY uk_uc_user_phone (phone)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '统一用户中心用户表';
