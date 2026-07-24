# 海外热点聚合观察台一期技术设计

## 1. 文档定位

本文是海外热点聚合观察台一期的技术设计，承接 [一期需求分析文档](phase-1-requirement-analysis.md) 和 [一期任务文档](phase-1-task-plan.md)，用于指导后端模块、前端应用、数据表、采集任务和图表接口的实现。

本文优先解决“怎样实现一期闭环”，不提前设计大规模实时计算、多租户计费和复杂推荐系统。后续如果新增数据源或扩大采集规模，应先更新本文中的数据契约、限流策略和验收口径。

## 2. 一期技术结论

| 项目 | 一期结论 |
| --- | --- |
| 后端模块 | `backend/vcoding-global-trend-monitor`，独立 Spring Boot Maven 子模块 |
| 前端应用 | `frontend/apps/global-trend-monitor-web`，独立 Vue 3 应用 |
| 数据库 | MySQL 8，Flyway 维护迁移脚本，MyBatis-Plus 访问 |
| 缓存 | 一期只用于短期任务锁、限流和重复触发保护，不缓存完整热点数据 |
| 认证 | 复用 `vcoding-auth`、`vcoding-gateway` 和 `vcoding-common`，不建立独立登录体系 |
| 首批数据源 | YouTube、GitHub |
| 采集方式 | 官方 API、开放接口或明确允许的 RSS/开放数据；按数据源 connector 隔离 |
| 任务方式 | 手动触发 + 每日定时，单数据源失败不阻断其他数据源 |
| 图表方案 | 前端优先评估 Apache ECharts；后端提供统一、可比较的聚合指标 |
| 敏感配置 | API Key、Token 只从环境变量或外部配置注入，不入库、不返回前端、不写日志 |

## 3. 总体架构

```text
浏览器
  ↓ HttpOnly Cookie
auth-web / global-trend-monitor-web
  ↓ /api
vcoding-gateway
  ├─ 校验登录 Cookie
  ├─ 注入签名后的内部用户头
  └─ 路由到业务模块
      ↓
vcoding-global-trend-monitor
  ├─ api：Controller、请求和响应对象
  ├─ application：查询、采集、评分、聚合、归档编排
  ├─ domain：热点、数据源、采集器、评分规则等领域模型
  ├─ infrastructure：MyBatis-Plus、外部 API、任务调度、缓存
  └─ config：数据源、任务、内部 Gateway 鉴权和 OpenAPI 配置
      ├─ MySQL：热点、指标快照、日聚合、用户操作、任务日志
      └─ 外部平台：YouTube / GitHub
```

### 3.1 后端包结构

```text
com.vcoding.globaltrend
├── VcodingGlobalTrendApplication.java
├── api
│   ├── AnalyticsController.java
│   ├── TrendController.java
│   ├── SourceController.java
│   ├── CollectJobController.java
│   └── dto/
├── application
│   ├── analytics/
│   ├── collect/
│   ├── source/
│   ├── trend/
│   └── useraction/
├── domain
│   ├── analytics/
│   ├── collect/
│   ├── source/
│   ├── trend/
│   └── useraction/
├── infrastructure
│   ├── external/
│   │   ├── youtube/
│   │   └── github/
│   ├── persistence/
│   │   ├── entity/
│   │   └── mapper/
│   └── scheduler/
└── config/
```

### 3.2 前端目录

```text
frontend/apps/global-trend-monitor-web/src/
├── api/
│   ├── analytics.ts
│   ├── collect-job.ts
│   ├── source.ts
│   └── trend.ts
├── components/
│   ├── AnalyticsChartPanel.vue
│   ├── TrendFilterBar.vue
│   ├── TrendList.vue
│   └── TrendMetricSummary.vue
├── router/
├── stores/
├── styles/
├── types/
│   ├── analytics.ts
│   ├── source.ts
│   └── trend.ts
└── views/
    ├── AnalyticsView.vue
    ├── CollectJobView.vue
    ├── SourceConfigView.vue
    ├── TrendDetailView.vue
    └── TrendHomeView.vue
```

前端请求复用 `@vcoding/auth-client` 的 Axios 封装。类型与 API 函数分离；后续接口稳定后，再将 OpenAPI 生成类型沉淀到 `frontend/packages/api-client`。

## 4. 认证、权限和数据隔离

### 4.1 请求认证

所有业务接口都经过 Gateway。业务模块启用现有内部 Gateway 用户头校验，并在 Controller 中通过 `@CurrentLoginUser CurrentUser currentUser` 获取当前用户，不直接解析 JWT。

业务模块需要复用以下能力：

- `vcoding-common` 的 `InternalGatewayUserFilter`。
- `GatewayUserHeaderService` 和内部鉴权配置。
- `ApiResponse`、`PageResponse` 和全局异常处理。
- 统一的 `traceId` 返回和日志链路。

### 4.2 权限范围

一期只区分“登录用户”和“管理员/系统运维”：

- 登录用户：查看热点、查看图表、收藏、忽略、标注和归档自己的操作记录。
- 管理员：配置数据源、手动触发任务、查看任务日志、启用或停用数据源。

热点、指标和日聚合属于系统公共数据，不按用户复制。用户操作表必须带 `user_id`，查询、更新和删除都按当前用户隔离。

### 4.3 数据源密钥

数据源配置分为非敏感配置和敏感配置：

- 非敏感配置：平台标识、显示名称、启用状态、地区、语言、关键词、采集频率、排序等，可入 `gtm_source.config_json`。
- 敏感配置：API Key、Access Token、Client Secret，不入 MySQL；从环境变量、密钥管理服务或部署平台 Secret 注入。

代码中使用数据源标识读取对应配置，例如 `GTM_YOUTUBE_API_KEY`。日志只记录数据源、任务 ID、HTTP 状态和错误类型，不记录请求头、完整 URL 查询参数或密钥。

## 5. 领域模型与状态

### 5.1 数据源 `Source`

```text
id, code, name, platformType, contentType, enabled,
region, language, config, lastSuccessAt, lastFailureAt
```

`code` 是稳定的机器标识，例如 `youtube`、`github`。`platformType` 用于图表分组，`contentType` 用于列表筛选。

数据源状态：

```text
ENABLED → DISABLED
ENABLED → ERROR_DEGRADED
ERROR_DEGRADED → ENABLED
```

连续失败不自动删除数据源。达到失败阈值后标记为 `ERROR_DEGRADED`，等待管理员查看任务日志后恢复或停用。

### 5.2 热点 `TrendItem`

热点是跨平台统一展示对象，必须同时保留平台身份和原始链接：

```text
id, sourceId, platformItemId, canonicalUrl,
titleOriginal, titleZh, summaryZh, authorName,
contentType, topicCode, keywords, region, language,
publishedAt, firstSeenAt, lastSeenAt,
heatScore, heatScoreUpdatedAt, status
```

唯一性建议：`source_id + platform_item_id` 唯一；`canonical_url` 作为辅助去重键。平台没有稳定 ID 时，使用规范化 URL 加内容指纹生成 `platform_item_id`。

热点状态：

```text
ACTIVE → HIDDEN
ACTIVE → EXPIRED
```

隐藏只影响展示，不删除原始数据。超过展示窗口的热点标记为 `EXPIRED`，历史数据仍可用于趋势聚合。

### 5.3 指标快照 `MetricSnapshot`

原始平台指标不能直接跨平台相加，统一以快照方式保存：

```text
trendItemId, capturedAt,
viewCount, likeCount, commentCount,
score, forkCount, starCount, replyCount,
rawMetricsJson
```

不同平台可用指标不同，缺失字段为 `NULL`，不能用 0 伪造。`raw_metrics_json` 用于保留一期暂未建模但公开且允许存储的指标，内容需经过大小限制和敏感字段过滤。

### 5.4 用户操作 `UserTrendAction`

一期支持以下操作：

```text
FAVORITE  收藏
IGNORE    忽略
ARCHIVE   归档
NOTE      备注
```

同一用户对同一热点的操作使用 `(user_id, trend_item_id, action_type)` 唯一约束。收藏和忽略是互斥展示状态，归档不影响热点公共榜单。

### 5.5 采集任务 `CollectJob`

```text
PENDING → RUNNING → SUCCESS
                    ↘ PARTIAL_SUCCESS
                    ↘ FAILED
RUNNING → CANCELED
```

任务记录数据源、触发方式、开始和结束时间、拉取数量、插入数量、更新数量、去重数量、失败数量、错误摘要和 traceId。任务执行必须具备幂等性，重复触发不能重复插入热点。

## 6. 数据库设计

一期建议使用 `V1__create_global_trend_monitor.sql` 建立基础表，后续字段变化继续按 Flyway 版本递增。表名统一使用 `gtm_` 前缀，字符集使用 `utf8mb4`。

### 6.1 `gtm_source` 数据源表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键 |
| `code` | VARCHAR(64) | 稳定数据源编码，唯一 |
| `name` | VARCHAR(120) | 展示名称 |
| `platform_type` | VARCHAR(32) | `VIDEO`、`COMMUNITY`、`CODE` 等 |
| `content_type` | VARCHAR(32) | `VIDEO`、`POST`、`REPOSITORY`、`NEWS` |
| `enabled` | TINYINT | 是否启用 |
| `status` | VARCHAR(32) | `ENABLED`、`DISABLED`、`ERROR_DEGRADED` |
| `region` | VARCHAR(32) | 默认地区 |
| `language` | VARCHAR(16) | 默认语言 |
| `config_json` | JSON | 非敏感采集配置 |
| `last_success_at` | DATETIME | 最近成功时间 |
| `last_failure_at` | DATETIME | 最近失败时间 |
| `created_at` / `updated_at` | DATETIME | 审计时间 |

索引：`uk_gtm_source_code`、`idx_gtm_source_enabled_status`。

### 6.2 `gtm_trend_item` 热点表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键 |
| `source_id` | BIGINT | 数据源 ID |
| `platform_item_id` | VARCHAR(255) | 平台内容 ID |
| `canonical_url` | VARCHAR(1000) | 规范化原文链接 |
| `title_original` | VARCHAR(500) | 原始标题 |
| `title_zh` | VARCHAR(500) | 中文标题，可为空 |
| `summary_zh` | TEXT | 中文摘要，可为空 |
| `author_name` | VARCHAR(255) | 作者或频道 |
| `content_type` | VARCHAR(32) | 内容类型 |
| `topic_code` | VARCHAR(64) | 主题编码，可为空 |
| `keywords_json` | JSON | 关键词数组 |
| `region` / `language` | VARCHAR | 来源地区和语言 |
| `published_at` | DATETIME | 平台发布时间 |
| `first_seen_at` / `last_seen_at` | DATETIME | 首次/最近采集时间 |
| `heat_score` | DECIMAL(12,4) | 当前综合热度分 |
| `heat_score_updated_at` | DATETIME | 热度更新时间 |
| `status` | VARCHAR(32) | `ACTIVE`、`HIDDEN`、`EXPIRED` |
| `created_at` / `updated_at` | DATETIME | 审计时间 |

索引：`uk_gtm_item_source_platform`、`idx_gtm_item_published`、`idx_gtm_item_source_heat`、`idx_gtm_item_topic_published`。

### 6.3 `gtm_metric_snapshot` 指标快照表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键 |
| `trend_item_id` | BIGINT | 热点 ID |
| `captured_at` | DATETIME | 指标采集时间 |
| `view_count` | BIGINT | 播放/浏览数 |
| `like_count` | BIGINT | 点赞数 |
| `comment_count` | BIGINT | 评论数 |
| `score` | DECIMAL(14,4) | 社区评分或平台分数 |
| `fork_count` | BIGINT | Fork 数 |
| `star_count` | BIGINT | Star 数 |
| `reply_count` | BIGINT | 回复数 |
| `raw_metrics_json` | JSON | 其他公开指标 |

索引：`idx_gtm_snapshot_item_captured`、`idx_gtm_snapshot_captured`。相同热点在同一采集任务内只保留一份快照。

### 6.4 `gtm_trend_daily_aggregate` 日聚合表

该表只保存用于图表的规范化聚合结果，避免每次打开图表都扫描全部快照。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键 |
| `stat_date` | DATE | 统计日期 |
| `source_id` | BIGINT | 数据源，可为空表示全平台 |
| `topic_code` | VARCHAR(64) | 主题，可为空表示全主题 |
| `content_type` | VARCHAR(32) | 内容类型，可为空表示全部 |
| `item_count` | INT | 热点数量 |
| `avg_heat_score` | DECIMAL(12,4) | 平均综合热度 |
| `max_heat_score` | DECIMAL(12,4) | 最高综合热度 |
| `growth_rate` | DECIMAL(12,6) | 相对上一比较周期的增长率 |
| `rank_score` | DECIMAL(12,4) | 用于榜单排序的聚合分 |
| `created_at` / `updated_at` | DATETIME | 审计时间 |

唯一键建议为 `stat_date + source_id + topic_code + content_type`，空值统一使用特殊值或拆分聚合层级，避免 MySQL 唯一索引对 `NULL` 的多行放行行为造成重复数据。

### 6.5 `gtm_user_trend_action` 用户操作表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键 |
| `user_id` | BIGINT | 用户 ID |
| `trend_item_id` | BIGINT | 热点 ID |
| `action_type` | VARCHAR(32) | 操作类型 |
| `note` | VARCHAR(1000) | 用户备注 |
| `created_at` / `updated_at` | DATETIME | 操作时间 |

索引：`uk_gtm_action_user_item_type`、`idx_gtm_action_user_type_updated`。

### 6.6 `gtm_collect_job` 采集任务表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键 |
| `source_id` | BIGINT | 数据源 ID |
| `trigger_type` | VARCHAR(32) | `MANUAL`、`SCHEDULED`、`RETRY` |
| `status` | VARCHAR(32) | 任务状态 |
| `started_at` / `finished_at` | DATETIME | 执行时间 |
| `fetched_count` | INT | 拉取数量 |
| `inserted_count` | INT | 新增数量 |
| `updated_count` | INT | 更新数量 |
| `deduplicated_count` | INT | 去重数量 |
| `failed_count` | INT | 单条失败数量 |
| `error_summary` | VARCHAR(2000) | 脱敏后的错误摘要 |
| `trace_id` | VARCHAR(64) | 链路 ID |
| `created_at` / `updated_at` | DATETIME | 审计时间 |

索引：`idx_gtm_job_source_created`、`idx_gtm_job_status_created`。

## 7. 数据源 connector 设计

### 7.1 统一接口

```java
public interface TrendSourceConnector {

    String sourceCode();

    FetchResult fetch(FetchContext context);
}
```

中间对象只表达平台无关字段，避免 Controller 或持久化层直接依赖第三方 SDK：

```text
TrendItemDraft
├── sourceCode
├── platformItemId
├── canonicalUrl
├── titleOriginal
├── authorName
├── contentType
├── publishedAt
├── region
├── language
├── metrics
└── rawPayload
```

`rawPayload` 只在内存中短暂存在，经过字段清洗和大小限制后才进入 `raw_metrics_json`；默认不保存完整第三方响应。

### 7.2 connector 责任边界

connector 只负责：

- 组装平台请求。
- 设置超时、分页和平台限流参数。
- 解析平台响应。
- 转换为 `TrendItemDraft`。
- 返回平台错误类型和可重试标记。

connector 不负责：

- 写入业务表。
- 计算跨平台热度分。
- 处理用户收藏。
- 生成中文摘要。
- 直接决定热点是否展示。

### 7.3 两个首批 connector

| 数据源 | 主要内容 | 一期采集参数 | 重点指标 |
| --- | --- | --- | --- |
| YouTube | 视频 | 地区、分类、时间窗口、分页 | 播放、点赞、评论 |
| GitHub | 仓库 | 编程语言、自然语言、日/周/月时间范围 | Star、Fork、周期新增 Star |

采集参数由 `gtm_source.config_json` 管理，代码只提供默认值和校验，不把地区、语言、时间范围和分页大小硬编码在 Controller 中。GitHub 一期按官网 Trending HTML 页面低频解析实现，不使用非官方 JSON API。

### 7.4 失败和重试

- 连接超时、HTTP 429、HTTP 5xx：允许有限次数指数退避重试。
- HTTP 401、403、平台权限不足：不自动重试，记录配置错误。
- 响应字段不完整：单条记录失败并继续处理其他记录。
- 平台返回空数据：任务可以成功，但记录数量为 0，并在日志中标记空结果。
- 超过单次最大页数或最大条数：正常结束，避免无限采集。

## 8. 去重、热度和聚合规则

### 8.1 去重顺序

1. 使用 `sourceCode + platformItemId` 做精确去重。
2. 没有平台 ID 时使用规范化 URL 去重：移除追踪参数、统一协议和尾部斜杠。
3. URL 不可用时使用标题指纹 + 作者 + 发布时间日做弱去重。
4. 标题相似度只用于人工检查或低置信度提示，不直接覆盖两条内容。

### 8.2 原始指标和规范化指标

原始指标只展示在来源详情中。跨平台图表只使用以下规范化字段：

- `item_count`：热点数量。
- `avg_heat_score`：平均综合热度。
- `max_heat_score`：最高综合热度。
- `growth_rate`：与上一比较周期相比的相对增长率。
- `rank_score`：用于排序的聚合分。

播放数、点赞数、评论数、Star 数和社区分数属于不同平台语义，不能直接相加或放在同一坐标轴比较。

### 8.3 一期热度分

一期先使用可解释的规则分，后续再根据人工标注和使用数据调参。不同内容类型使用不同指标映射，但最终统一到 0 到 100 分：

```text
热度分 = 100 × 截断到 [0, 1] 的加权和

加权和 =
  0.35 × 新鲜度
  + 0.30 × 相对互动
  + 0.20 × 传播规模
  + 0.15 × 平台排名信号
```

具体规则：

- 新鲜度：按发布时间距当前时间的小时数指数衰减，超过展示窗口后趋近于 0。
- 相对互动：互动指标除以同平台、同内容类型的历史中位数，再做 `log1p` 和上下限截断。
- 传播规模：播放、浏览、Star 等可用规模指标按平台单独归一化，缺失时不补 0，而是按可用指标重新分配权重。
- 平台排名信号：平台明确提供榜单位置时转为区间分，没有榜单位置时为 0。

一期不把不同平台的原始数值直接比较。热度分中的平台归一化基线需记录版本号，例如 `score_version = v1`，以后调整公式时可以重新计算而不破坏历史解释。

### 8.4 日聚合

每日任务在采集完成后生成或更新前一日及当前日的聚合数据。对于近 7 天和近 30 天查询：

- 趋势折线使用日粒度聚合。
- 平台对比使用查询窗口内的热点数量、平均热度和最高热度。
- 增长率默认比较相邻等长窗口，例如近 7 天对比此前 7 天。
- 数据不足一个完整比较窗口时返回 `insufficient_data=true`，前端显示“数据积累中”，不展示误导性的增长率。

## 9. 接口设计

所有接口默认使用 `/api` 前缀，并返回现有统一结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "traceId": "..."
}
```

### 9.1 热点接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/global-trend/trends` | 分页查询热点 |
| `GET` | `/api/global-trend/trends/{id}` | 查询热点详情 |
| `GET` | `/api/global-trend/trends/{id}/snapshots` | 查询指标快照 |
| `GET` | `/api/global-trend/trends/search` | 关键词搜索 |
| `POST` | `/api/global-trend/trends/{id}/actions` | 收藏、忽略、归档、备注 |
| `DELETE` | `/api/global-trend/trends/{id}/actions/{type}` | 删除当前用户指定操作 |
| `POST` | `/api/global-trend/trends/{id}/convert-topic` | 转为内容平台选题 |

热点列表参数：

```text
pageNo, pageSize, dateFrom, dateTo,
sourceCode, topicCode, contentType, region, language,
actionType, keyword, sortBy, sortDirection
```

`sortBy` 只允许白名单值：`heatScore`、`publishedAt`、`lastSeenAt`、`rankChange`。

热点列表响应中的指标建议区分：

```json
{
  "id": 101,
  "source": { "code": "youtube", "name": "YouTube" },
  "titleOriginal": "Original title",
  "titleZh": "中文标题",
  "summaryZh": "中文摘要",
  "heatScore": 82.31,
  "rankChange": 3,
  "metrics": {
    "viewCount": 120000,
    "likeCount": 4200,
    "commentCount": 310
  },
  "userActions": ["FAVORITE"],
  "publishedAt": "2026-07-14T09:00:00+08:00",
  "lastSeenAt": "2026-07-14T12:00:00+08:00"
}
```

### 9.2 图表接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/global-trend/analytics/overview` | 总览指标和更新时间 |
| `GET` | `/api/global-trend/analytics/trend-lines` | 按日趋势折线数据 |
| `GET` | `/api/global-trend/analytics/platform-comparison` | 平台对比数据 |
| `GET` | `/api/global-trend/analytics/topic-distribution` | 主题分布数据 |
| `GET` | `/api/global-trend/analytics/ranking-changes` | 热点升降榜 |
| `GET` | `/api/global-trend/analytics/heatmap` | 主题 × 日期热力图 |

统一参数：

```text
dateFrom, dateTo, sourceCodes[], topicCodes[], contentTypes[],
region, language, limit
```

统一返回元数据：

```json
{
  "dateFrom": "2026-07-08",
  "dateTo": "2026-07-14",
  "generatedAt": "2026-07-14T12:05:00+08:00",
  "dataAsOf": "2026-07-14T12:00:00+08:00",
  "insufficientData": false,
  "series": []
}
```

示例：趋势折线的 `series` 只返回同一单位的规范化分数：

```json
{
  "series": [
    {
      "key": "youtube",
      "name": "YouTube",
      "metric": "avgHeatScore",
      "unit": "score_0_100",
      "points": [
        { "date": "2026-07-08", "value": 61.2 },
        { "date": "2026-07-09", "value": 64.8 }
      ]
    }
  ]
}
```

### 9.3 数据源和任务接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/global-trend/sources` | 管理员 | 查询数据源状态和非敏感配置 |
| `PUT` | `/api/global-trend/sources/{code}` | 管理员 | 更新启用状态和采集参数 |
| `POST` | `/api/global-trend/sources/{code}/collect` | 管理员 | 手动触发采集 |
| `POST` | `/api/global-trend/sources/{code}/test` | 管理员 | 小样本验证外部接口，不写入热点表 |
| `GET` | `/api/global-trend/collect-jobs` | 管理员 | 分页查询任务日志 |
| `GET` | `/api/global-trend/collect-jobs/{id}` | 管理员 | 查询任务详情 |

接口绝不返回 API Key、Access Token、Client Secret 等敏感配置；更新接口也只接受脱敏后的非敏感配置。

## 10. 图表和前端交互设计

### 10.1 页面和图表对应关系

| 页面 | 首屏信息 | 图表/交互 |
| --- | --- | --- |
| 今日热点 | 今日热点列表、平台筛选、采集更新时间 | 小型趋势折线、热点升降榜 |
| 平台榜单 | 单平台榜单和原始指标 | 平台内热度趋势、排行变化 |
| 数据洞察 | 跨平台、跨主题比较 | 趋势折线、柱状图、分布图、热力图 |
| 热点详情 | 原始指标、摘要、原文和历史指标 | 单条指标快照折线 |
| 采集任务 | 数据源健康、失败和延迟 | 任务状态筛选，不强制图表化 |

### 10.2 图表交互

- 日期范围统一支持今日、昨日、近 7 天、近 30 天，并允许自定义范围。
- 图表标题必须显示指标名称、单位和数据时间范围。
- 悬停显示具体日期、平台、指标值和数据更新时间。
- 点击平台或主题后联动列表筛选；点击单个热点进入详情。
- 数据不足时显示空状态或“数据积累中”，不能用 0 画出虚假趋势。
- 图表颜色遵守平台视觉规范：主色使用蓝色，状态或增长可使用青绿色，风险/失败使用语义色；不要用大面积渐变。
- 图表容器使用轻边框和稳定高度，不使用多层卡片嵌套。

### 10.3 图表口径

图表接口不得把以下指标放在同一条折线或同一坐标轴中：播放数、点赞数、评论数、Star 数、Fork 数、社区分数。若产品需要同时查看，使用分面图、独立指标卡或单条热点详情中的多指标切换。

平台比较默认展示：热点数、平均热度、最高热度、增长率。主题分布默认展示：热点数占比和主题平均热度。排名变化默认展示：当前排名、上一周期排名和变化名次。

## 11. 调度、并发和可观测性

### 11.1 调度

- 每日定时任务按数据源依次或受控并发执行。
- 单个数据源同一时间只允许一个运行中的任务。
- 使用 Redis 锁或数据库唯一运行约束防止多实例重复执行。
- 定时任务时区固定为 `Asia/Shanghai`，并在后台配置中明确记录。
- 定时任务失败后不无限重试；失败任务进入日志，由人工或受控重试处理。

### 11.2 日志和指标

每个任务至少记录：`traceId`、`jobId`、`sourceCode`、请求耗时、HTTP 状态、分页次数、拉取数量、入库数量、去重数量和失败数量。

日志中禁止出现：API Key、Token、Cookie、完整第三方响应、用户私密信息和未脱敏请求参数。

一期先使用结构化应用日志和任务表满足排查需求；后续接入统一监控时，再增加任务耗时、成功率、429 次数和字段缺失率指标。

## 12. 合规和内容存储边界

- 优先使用官方 API、RSS、开放接口和授权数据源。
- 不绕过登录、验证码、付费墙、反爬限制或签名校验。
- 不采集私信、封闭社群和非公开内容。
- 默认保存标题、摘要、公开指标、作者/频道名称、原文链接和必要元数据。
- 原文内容只做链接跳转，不在一期复制整篇文章、完整视频或评论区全文。
- 数据源 connector 必须在实现说明中记录官方入口、限流方式、权限要求和可保存字段。
- 图表和摘要应保留原文链接及数据更新时间，避免把 AI 摘要或聚合分数误认为平台官方结论。

## 13. 一期实现顺序

1. 创建 Maven 子模块和 Vue 应用骨架，接入 Gateway 内部鉴权。
2. 创建 Flyway 基础表和 MyBatis-Plus 实体、Mapper。
3. 先完成 GitHub 和 YouTube connector。
4. 完成采集任务、幂等入库、指标快照和热度分。
5. 完成热点列表、详情、收藏和任务日志。
6. 完成日聚合和图表接口，再封装前端图表组件。
7. 最后接入中文标题、摘要和主题分类，避免 AI 生成链路阻塞基础数据闭环。

## 14. 技术验收清单

- [ ] 新后端模块已加入父 `pom.xml`，并能通过 `mvn -f backend/pom.xml validate`。
- [ ] 新前端应用遵守 pnpm workspace 和现有 Vue 目录约定。
- [ ] 未登录请求会通过统一登录页回跳，业务模块不重复实现登录。
- [ ] Gateway 用户头校验和用户数据隔离有效。
- [ ] Flyway 可以在空库完成一期表结构创建。
- [ ] YouTube、GitHub 采集失败互不影响，重复执行不重复插入。
- [ ] 原始指标和规范化指标分开保存，图表接口没有混合不可比原始指标。
- [ ] 热度分包含版本号或等价的可追溯规则标识。
- [ ] 图表返回数据时间、更新时间和数据不足标记。
- [ ] API Key、Token、Cookie 和完整第三方响应不会进入日志或前端响应。
- [ ] 手动采集、定时采集、失败重试和任务日志可被人工验收。
