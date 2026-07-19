CREATE TABLE gtm_source
(
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '数据源主键',
    code              VARCHAR(64)  NOT NULL COMMENT '稳定数据源编码',
    name              VARCHAR(120) NOT NULL COMMENT '数据源名称',
    platform_type     VARCHAR(32)  NOT NULL COMMENT '平台类型：VIDEO、COMMUNITY、CODE 等',
    content_type      VARCHAR(32)  NOT NULL COMMENT '内容类型：VIDEO、POST、REPOSITORY、NEWS 等',
    enabled           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：0否，1是',
    status            VARCHAR(32)  NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED、DISABLED、ERROR_DEGRADED',
    region            VARCHAR(32)           DEFAULT NULL COMMENT '默认地区',
    language          VARCHAR(16)           DEFAULT NULL COMMENT '默认语言',
    config_json       JSON                  DEFAULT NULL COMMENT '非敏感采集配置',
    last_success_at   DATETIME              DEFAULT NULL COMMENT '最近成功采集时间',
    last_failure_at   DATETIME              DEFAULT NULL COMMENT '最近失败时间',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gtm_source_code (code),
    KEY idx_gtm_source_enabled_status (enabled, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '海外热点数据源配置表';

CREATE TABLE gtm_trend_item
(
    id                    BIGINT         NOT NULL AUTO_INCREMENT COMMENT '热点主键',
    source_id             BIGINT         NOT NULL COMMENT '数据源 ID',
    platform_item_id      VARCHAR(255)   NOT NULL COMMENT '平台内容 ID或内容指纹',
    canonical_url         VARCHAR(1000)  NOT NULL COMMENT '规范化原文链接',
    title_original        VARCHAR(500)   NOT NULL COMMENT '原始标题',
    title_zh              VARCHAR(500)            DEFAULT NULL COMMENT '中文标题',
    summary_zh            TEXT                    DEFAULT NULL COMMENT '中文摘要',
    author_name           VARCHAR(255)            DEFAULT NULL COMMENT '作者、频道或发布主体',
    content_type          VARCHAR(32)    NOT NULL COMMENT '内容类型',
    topic_code            VARCHAR(64)             DEFAULT NULL COMMENT '主题编码',
    keywords_json         JSON                    DEFAULT NULL COMMENT '关键词数组',
    region                VARCHAR(32)             DEFAULT NULL COMMENT '来源地区',
    language              VARCHAR(16)             DEFAULT NULL COMMENT '内容语言',
    published_at          DATETIME                DEFAULT NULL COMMENT '平台发布时间',
    first_seen_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次采集时间',
    last_seen_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近采集时间',
    heat_score            DECIMAL(12, 4) NOT NULL DEFAULT 0 COMMENT '综合热度分，范围 0 到 100',
    score_version         VARCHAR(32)    NOT NULL DEFAULT 'v1' COMMENT '热度评分规则版本',
    heat_score_updated_at DATETIME                DEFAULT NULL COMMENT '热度更新时间',
    status                VARCHAR(32)    NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE、HIDDEN、EXPIRED',
    created_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gtm_item_source_platform (source_id, platform_item_id),
    KEY idx_gtm_item_published (published_at),
    KEY idx_gtm_item_source_heat (source_id, heat_score, published_at),
    KEY idx_gtm_item_topic_published (topic_code, published_at),
    KEY idx_gtm_item_status_seen (status, last_seen_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '海外热点内容表';

CREATE TABLE gtm_metric_snapshot
(
    id               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '指标快照主键',
    trend_item_id    BIGINT         NOT NULL COMMENT '热点 ID',
    captured_at      DATETIME       NOT NULL COMMENT '指标采集时间',
    view_count       BIGINT                  DEFAULT NULL COMMENT '播放或浏览数',
    like_count       BIGINT                  DEFAULT NULL COMMENT '点赞数',
    comment_count    BIGINT                  DEFAULT NULL COMMENT '评论数',
    score            DECIMAL(14, 4)           DEFAULT NULL COMMENT '社区评分或平台分数',
    fork_count      BIGINT                  DEFAULT NULL COMMENT 'Fork 数',
    star_count       BIGINT                  DEFAULT NULL COMMENT 'Star 数',
    reply_count      BIGINT                  DEFAULT NULL COMMENT '回复数',
    raw_metrics_json JSON                    DEFAULT NULL COMMENT '其他公开指标，不保存完整第三方响应',
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gtm_snapshot_item_captured (trend_item_id, captured_at),
    KEY idx_gtm_snapshot_captured (captured_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '海外热点指标快照表';

CREATE TABLE gtm_trend_daily_aggregate
(
    id             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '日聚合主键',
    stat_date      DATE           NOT NULL COMMENT '统计日期',
    source_id      BIGINT         NOT NULL DEFAULT 0 COMMENT '数据源 ID，0表示全平台',
    topic_code     VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '主题编码，空字符串表示全部主题',
    content_type   VARCHAR(32)    NOT NULL DEFAULT '' COMMENT '内容类型，空字符串表示全部类型',
    item_count     INT            NOT NULL DEFAULT 0 COMMENT '热点数量',
    avg_heat_score DECIMAL(12, 4) NOT NULL DEFAULT 0 COMMENT '平均综合热度',
    max_heat_score DECIMAL(12, 4) NOT NULL DEFAULT 0 COMMENT '最高综合热度',
    growth_rate    DECIMAL(12, 6)          DEFAULT NULL COMMENT '相对比较周期的增长率',
    rank_score     DECIMAL(12, 4) NOT NULL DEFAULT 0 COMMENT '用于榜单排序的聚合分',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gtm_daily_aggregate_dimension (stat_date, source_id, topic_code, content_type),
    KEY idx_gtm_daily_aggregate_date (stat_date),
    KEY idx_gtm_daily_aggregate_source_date (source_id, stat_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '海外热点日聚合统计表';

CREATE TABLE gtm_user_trend_action
(
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '用户操作主键',
    user_id        BIGINT        NOT NULL COMMENT '用户 ID，用于数据隔离',
    trend_item_id  BIGINT        NOT NULL COMMENT '热点 ID',
    action_type    VARCHAR(32)   NOT NULL COMMENT '操作类型：FAVORITE、IGNORE、ARCHIVE、NOTE、TAG',
    note           VARCHAR(1000)          DEFAULT NULL COMMENT '用户备注',
    tags_json      JSON                   DEFAULT NULL COMMENT '用户标签数组',
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gtm_action_user_item_type (user_id, trend_item_id, action_type),
    KEY idx_gtm_action_user_type_updated (user_id, action_type, updated_at),
    KEY idx_gtm_action_item (trend_item_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户热点收藏、标注和归档操作表';

CREATE TABLE gtm_collect_job
(
    id                   BIGINT         NOT NULL AUTO_INCREMENT COMMENT '采集任务主键',
    source_id            BIGINT         NOT NULL COMMENT '数据源 ID',
    trigger_type         VARCHAR(32)    NOT NULL COMMENT '触发方式：MANUAL、SCHEDULED、RETRY',
    status               VARCHAR(32)    NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING、RUNNING、SUCCESS、PARTIAL_SUCCESS、FAILED、CANCELED',
    started_at           DATETIME                DEFAULT NULL COMMENT '开始时间',
    finished_at          DATETIME                DEFAULT NULL COMMENT '结束时间',
    fetched_count        INT            NOT NULL DEFAULT 0 COMMENT '拉取数量',
    inserted_count       INT            NOT NULL DEFAULT 0 COMMENT '新增数量',
    updated_count        INT            NOT NULL DEFAULT 0 COMMENT '更新数量',
    deduplicated_count   INT            NOT NULL DEFAULT 0 COMMENT '去重数量',
    failed_count         INT            NOT NULL DEFAULT 0 COMMENT '失败数量',
    error_summary        VARCHAR(2000)           DEFAULT NULL COMMENT '脱敏后的错误摘要',
    trace_id             VARCHAR(64)             DEFAULT NULL COMMENT '链路 ID',
    created_at           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_gtm_job_source_created (source_id, created_at),
    KEY idx_gtm_job_status_created (status, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '海外热点采集任务表';

INSERT INTO gtm_source
    (code, name, platform_type, content_type, enabled, status, region, language, config_json)
VALUES
    ('youtube', 'YouTube', 'VIDEO', 'VIDEO', 1, 'ENABLED', 'US', 'en', JSON_OBJECT('chart', 'mostPopular')),
    ('hacker-news', 'Hacker News', 'COMMUNITY', 'POST', 1, 'ENABLED', NULL, 'en', JSON_OBJECT('feed', 'topstories')),
    ('github', 'GitHub', 'CODE', 'REPOSITORY', 1, 'ENABLED', NULL, 'en', JSON_OBJECT('sort', 'updated'));
