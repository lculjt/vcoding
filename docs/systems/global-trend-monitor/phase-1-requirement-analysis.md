# 海外热点聚合观察台一期需求分析文档

## 0. 文档定位

本文用于梳理一个新业务系统：海外热点聚合观察台。系统目标是帮助用户每天集中查看海外主流平台、开发者社区、新闻站点和内容社区的热点内容，辅助选题发现、趋势判断、竞品观察和内容生产。

本文只定义一期需求和边界，不直接承诺所有平台都能自动抓取。涉及平台数据时，优先使用官方 API、官方 RSS、开放数据集、授权第三方数据源或用户手动导入；不以绕过登录、验证码、反爬策略或平台访问限制为产品能力。

## 1. 项目背景

当前海外热点信息分散在 YouTube、X、Reddit、Hacker News、Product Hunt、GitHub、TikTok、新闻媒体和各类垂直社区。用户如果每天手动打开多个网站查看趋势，会遇到以下问题：

- 平台数量多，浏览成本高。
- 不同平台热度指标不同，难以横向比较。
- 热点内容生命周期短，错过后不易追溯。
- 原始内容语言以英文为主，中文用户理解和筛选成本较高。
- 仅看单个平台容易产生信息偏差，难以判断趋势是否跨平台扩散。
- 热点和内容生产、竞品分析、市场调研之间缺少结构化沉淀。

因此需要建设一个“海外热点聚合 + 趋势分析 + 内容归档”的工作台，将每日热点按平台、地区、主题、语言和热度进行统一组织。

## 2. 产品定位

平台定位为“海外热点聚合观察台”，不是内容搬运平台，也不是无限制爬虫系统。

核心价值：

```text
多源热点采集
  ↓
热度归一化与去重
  ↓
中文摘要与主题分类
  ↓
趋势榜单与搜索筛选
  ↓
收藏、标注、归档和选题转化
```

一期重点解决“每天看什么”和“哪些热点值得继续研究”。

二期以后再考虑 AI 深度分析、自动生成选题、竞品监控、趋势预警和与现有 AI 内容生产平台联动。

## 3. 目标用户

- 自媒体创作者：寻找海外热点选题、视频灵感、标题方向。
- 内容运营人员：跟踪海外社媒热点，判断是否适合本地化改写。
- 跨境电商和品牌市场团队：观察海外消费趋势、产品热词和竞品动向。
- 产品经理和创业者：跟踪 Product Hunt、Hacker News、GitHub 等技术和产品趋势。
- 投研、咨询和市场研究人员：关注跨平台热度变化和议题演化。
- AI 内容生产团队：把热点作为内容生成、脚本生成和多平台分发的上游输入。

## 4. 一期目标

一期目标是完成“可配置数据源 + 每日热点榜单 + 中文摘要 + 收藏归档”的最小闭环。

一期核心产出：

- 每日热点列表。
- 按平台、主题、地区、语言筛选。
- 热点详情页。
- 原文链接跳转。
- 中文摘要和关键词。
- 热度分、热度来源指标和采集时间。
- 趋势图表和跨平台数据比对。
- 收藏、忽略、标注和归档。
- 数据源配置和采集任务记录。

一期建议优先接入数据稳定、合规风险较低、官方入口清晰的平台，不把高风险网页抓取作为主路径。

## 5. 推荐关注的数据源

### 5.1 一期优先接入

| 平台/来源 | 关注内容 | 推荐接入方式 | 一期建议 | 备注 |
| --- | --- | --- | --- | --- |
| YouTube | 热门视频、分类热门、地区热门 | YouTube Data API `videos.list` 的 `chart=mostPopular` | 优先接入 | 可按 `regionCode` 和 `videoCategoryId` 获取热门视频，适合做每日视频热点榜。 |
| GitHub | 官网 Trending 仓库、近期升温项目、技术趋势 | GitHub Trending HTML 页面解析 | 优先接入 | GitHub 没有官方 Trending API；一期低频解析官网 Trending 页面，支持编程语言、自然语言和日/周/月范围筛选。 |
| Reddit | 子版块热门帖、Top 帖 | Reddit 官方 API | 谨慎优先 | 需要遵守 Reddit 数据 API 条款、认证和限流；建议先接少量公开 subreddit。 |
| Product Hunt | 每日新产品、投票热门产品 | Product Hunt API v2 | 谨慎优先 | 对产品、SaaS、AI 工具选题价值高；需确认 token、限流和商业使用条件。 |
| Mastodon | 趋势标签、趋势帖子、趋势链接 | Mastodon Trends API | 可接入 | 需要选择实例，数据覆盖取决于实例。 |
| NewsAPI / GDELT | 国际新闻、媒体报道热点 | 官方 API 或开放数据接口 | 可接入 | 适合补齐新闻侧热点，注意商业授权和免费额度。 |

### 5.2 二期调研接入

| 平台/来源 | 关注内容 | 接入难点 | 建议 |
| --- | --- | --- | --- |
| X | 热门帖、关键词搜索、账号动态 | API 付费和权限变化较快；趋势能力和额度需按当前套餐确认 | 一期先做链接收藏、关键词人工导入或少量官方搜索 API 调研，不把 X 作为唯一核心来源。 |
| Hacker News | 技术新闻、创业、开发者讨论 | 数据开放度高，但一期平台数量收缩后暂不接入 | 二期可优先恢复，沿用已验证过的官方 Firebase API 或 Algolia HN API。 |
| TikTok | 海外短视频热点、音乐和标签趋势 | 公开热点能力有限，Research API 有资格和用途限制 | 二期按官方 Research API 或授权第三方数据源调研。 |
| Bluesky | 社交讨论、关键词帖子 | 生态仍在演进，热榜能力需要结合搜索、feed 或 firehose 自建 | 二期接入关键词监控或垂直 feed。 |
| Google Trends | 搜索趋势、地区热搜 | Google 没有稳定通用的公开官方 API；第三方库存在稳定性风险 | 一期不作为自动采集主源，可作为人工参考或后续第三方数据源。 |
| LinkedIn / Instagram / Facebook | 职场、品牌和社交热点 | 登录态、权限、反爬和商业条款限制明显 | 不建议一期抓取网页；如有需求，走官方开放平台或商业数据供应商。 |
| Discord / Telegram | 社群热点 | 多为半私域内容，授权和隐私边界复杂 | 仅在用户明确授权自己的频道或群组时接入。 |

### 5.3 可关注的垂直网站

除 YouTube、X 外，建议按业务目标关注以下海外站点：

- 技术与创业：Hacker News、GitHub、Product Hunt、Lobsters、Indie Hackers。
- AI 与开发者：Hugging Face Trending、Papers with Code、arXiv、OpenAI/Anthropic/Google DeepMind 官方博客。
- 新闻与宏观：Google News、BBC、CNN、Reuters、AP News、The Verge、TechCrunch、GDELT。
- 消费与品牌：TikTok、Instagram、Pinterest、Amazon Best Sellers、Etsy Trends。
- 投资与市场：Yahoo Finance、CoinMarketCap、CoinGecko、Seeking Alpha。
- 游戏与娱乐：Twitch、Steam Charts、IGN、Metacritic、Rotten Tomatoes。

一期不要一次性接入所有来源。当前一期只做 YouTube 和 GitHub，先把视频热点和开发者项目趋势的采集、展示、图表和归档闭环跑稳，再逐步扩展。

## 6. 功能范围

### 6.1 数据源管理

用户可以配置需要关注的平台和采集规则。

字段建议：

- 数据源名称。
- 平台类型：一期启用 YouTube、GitHub；系统枚举保留 X、Reddit、Hacker News、Product Hunt、News、Other 作为后续扩展。
- 接入方式：官方 API、RSS、开放数据集、第三方数据源、人工导入。
- 认证方式：无需认证、API Key、OAuth、Bearer Token。
- 采集范围：地区、频道、关键词、分类、子版块、账号、仓库语言等。
- 采集频率：每日、每 6 小时、每小时、手动。
- 启用状态。
- 限流配置。
- 最近采集时间。
- 失败次数和失败原因。

### 6.2 热点采集任务

系统按数据源配置定时采集热点内容。

一期采集策略：

- 默认每日采集一次。
- 支持手动触发单个数据源采集。
- 每次采集保存原始响应摘要，便于排查问题。
- 每条内容保留原文 URL、平台 ID、标题、作者、发布时间、采集时间和平台指标。
- 对同一 URL、同一平台 ID 或相似标题进行去重。
- 采集失败不影响其他数据源，任务记录失败原因。

不建议一期做：

- 登录用户态网页抓取。
- 绕过验证码、签名、风控或反爬限制。
- 大规模全站抓取。
- 抓取评论区全文和用户个人资料。

### 6.3 热点榜单

用户可以查看每日热点。

榜单筛选：

- 日期。
- 平台。
- 地区。
- 语言。
- 主题分类。
- 内容类型：视频、帖子、新闻、项目、产品、论文。
- 热度范围。
- 是否已收藏。
- 是否已归档。

排序方式：

- 综合热度。
- 平台原始热度。
- 发布时间。
- 采集时间。
- 增长速度。

列表展示字段：

- 标题。
- 平台和来源。
- 作者或发布主体。
- 原始发布时间。
- 热度分。
- 原始指标，例如播放数、点赞数、评论数、投票数、Star 数。
- 中文摘要。
- 关键词。
- 原文链接。
- 收藏状态。

### 6.4 热度计算

不同平台指标不可直接相加，需要做归一化。

一期建议采用可解释的规则分：

```text
综合热度分 = 平台基础分 + 互动分 + 新鲜度分 + 增长分 + 人工权重
```

字段说明：

- 平台基础分：不同平台设置基础权重。
- 互动分：播放、点赞、评论、转发、投票、Star 等指标归一化后计算。
- 新鲜度分：发布时间越近，分数越高。
- 增长分：与上次采集相比的指标增长。
- 人工权重：用户可对特定平台、关键词或来源增加权重。

一期必须保留平台原始指标，避免只展示一个不可解释的黑盒分数。

### 6.5 数据比对与趋势可视化

系统需要用图表和对比视图帮助用户更直观地判断热点变化，而不是只提供静态列表。

一期支持的比对维度：

- 时间比对：今日、昨日、近 7 天、近 30 天。
- 平台比对：不同平台的热点数量、平均热度、最高热度、增长速度。
- 主题比对：不同主题的热度分布、占比和变化趋势。
- 内容类型比对：视频、帖子、新闻、项目、产品等类型的热度差异。
- 地区和语言比对：不同地区、语言内容的热度分布。
- 单条热点比对：同一热点在多次采集中的指标变化。
- 排名变化比对：当前排名与上一次采集、昨日排名、近 7 天最高排名对比。

一期建议提供的图表：

- 趋势折线图：展示总热度、某平台热度、某主题热度随时间变化。
- 平台对比柱状图：展示各平台今日热点数量、平均热度和最高热度。
- 主题分布图：展示主题占比，可使用条形图或环形图，不建议使用过于装饰化的图形。
- 热点排名变化图：展示 Top 热点排名升降。
- 单条热点指标折线图：展示播放、点赞、评论、投票、Star 等指标的历史快照。
- 主题热力图：展示日期和主题两个维度下的热度变化。

图表交互要求：

- 支持按日期范围、平台、主题、地区、语言过滤。
- 鼠标悬停或点击时显示原始指标、归一化热度分、采集时间和排名变化。
- 图表中的数据点可以跳转到对应热点列表或热点详情。
- 空数据、采集失败、数据不足时要显示明确状态，不展示误导性空图。
- 图表必须同时保留“综合热度分”和“平台原始指标”的查看入口。

数据口径要求：

- 同一张图表只能比较同一时间窗口内的数据。
- 跨平台对比默认使用归一化热度分，不能直接把播放数、点赞数、Star 数相加。
- 原始指标只能在平台内部或单条热点详情中直接比较。
- 图表应标注数据更新时间，避免用户误判为实时数据。
- 一期以日级和采集批次级统计为主，不做秒级实时监控。

### 6.6 中文摘要与标签

系统为海外内容生成中文摘要，降低阅读成本。

一期支持：

- 标题翻译。
- 100 到 200 字中文摘要。
- 关键词提取。
- 主题分类。
- 情绪倾向：正向、中性、负向、争议。
- 内容类型识别。
- 是否适合转化为选题。

AI 生成内容必须标记生成时间和模型来源。摘要只作为辅助理解，不替代原文。

### 6.7 热点详情

用户点击热点进入详情页。

详情页内容：

- 原标题和中文标题。
- 平台、作者、发布时间、采集时间。
- 原文链接。
- 原始指标和历史指标趋势。
- 排名变化和同平台相似内容对比。
- 中文摘要。
- 关键词和主题。
- 相似热点。
- 跨平台关联内容。
- 用户笔记。
- 收藏、忽略、归档。
- 转为选题入口。

### 6.8 收藏、标注和归档

用户可以把热点沉淀为可复用资产。

操作：

- 收藏。
- 取消收藏。
- 标记为已读。
- 标记为忽略。
- 添加标签。
- 添加中文备注。
- 归档到主题库。
- 转为内容选题。

一期建议先完成收藏、标签、备注和归档，转为选题可先生成一条简单的选题记录，二期再与 AI 内容生产平台深度联动。

### 6.9 搜索与过滤

支持按关键词搜索已采集内容。

一期搜索范围：

- 原标题。
- 中文标题。
- 摘要。
- 作者。
- 平台。
- 标签。
- 用户备注。

可选过滤：

- 日期范围。
- 平台。
- 主题。
- 收藏状态。
- 归档状态。

### 6.10 数据源健康监控

系统展示采集任务运行状态。

监控字段：

- 数据源总数。
- 启用数据源数。
- 今日采集成功数。
- 今日采集失败数。
- 最近失败原因。
- API 配额使用情况。
- 下一次计划采集时间。

## 7. 页面规划

### 7.1 今日热点

默认首页，展示今日跨平台热点榜。

模块：

- 顶部筛选栏：日期、平台、地区、主题、语言。
- 概览图表区：今日热度走势、平台热度对比、主题分布。
- 今日综合榜。
- 平台榜单切换。
- 热点卡片或表格。
- 快速收藏、忽略、打开原文。

### 7.2 平台榜单

按单个平台查看热点。

一期平台：

- YouTube。
- GitHub。

### 7.3 数据洞察

展示跨平台和跨时间窗口的数据比对。

一期模块：

- 总热度趋势：按天展示总热度和热点数量。
- 平台对比：展示各平台热点数量、平均热度、最高热度和增长率。
- 主题分布：展示不同主题的热度占比和变化。
- Top 热点升降榜：展示排名上升、下降、新入榜和持续热门内容。
- 地区和语言分布：展示不同地区、语言内容的热点占比。
- 图表联动筛选：点击平台、主题或日期后联动刷新热点列表。

视觉要求：

- 图表应服务于分析，不做营销式大面积装饰。
- 图表颜色遵守平台视觉规范，以低饱和色和清晰对比为主。
- 信息密集区域优先使用表格、紧凑指标和可读图表，不堆叠大卡片。

### 7.4 热点详情

展示单条热点的完整信息、摘要、趋势、相似内容和用户标注。

### 7.5 收藏归档

展示用户收藏和归档的热点。

支持：

- 标签筛选。
- 备注编辑。
- 批量归档。
- 转为选题。

### 7.6 数据源配置

管理平台接入配置。

支持：

- 新增数据源。
- 编辑采集规则。
- 启用或停用。
- 测试连接。
- 手动采集。
- 查看最近任务。

### 7.7 采集任务日志

用于管理员排查采集问题。

展示：

- 任务 ID。
- 数据源。
- 开始时间。
- 结束时间。
- 状态。
- 拉取数量。
- 入库数量。
- 去重数量。
- 失败原因。

## 8. 数据对象初稿

### 8.1 数据源 Source

```text
source
├── id
├── name
├── platform
├── access_type
├── auth_type
├── config_json
├── schedule_cron
├── enabled
├── rate_limit_config
├── last_run_at
├── last_success_at
├── last_error_message
├── created_at
└── updated_at
```

### 8.2 热点内容 TrendItem

```text
trend_item
├── id
├── source_id
├── platform
├── platform_item_id
├── original_url
├── original_title
├── translated_title
├── author_name
├── published_at
├── collected_at
├── content_type
├── language
├── region
├── summary_zh
├── keywords_json
├── topics_json
├── raw_metrics_json
├── normalized_score
├── score_detail_json
├── raw_payload_digest
├── status
├── created_at
└── updated_at
```

### 8.3 指标快照 MetricSnapshot

```text
metric_snapshot
├── id
├── trend_item_id
├── snapshot_at
├── metrics_json
├── normalized_score
└── created_at
```

### 8.4 日聚合统计 TrendDailyAggregate

用于支撑趋势图、平台对比和主题分布，避免前端在图表查询时反复扫描明细数据。

```text
trend_daily_aggregate
├── id
├── aggregate_date
├── platform
├── topic
├── content_type
├── region
├── language
├── item_count
├── average_score
├── max_score
├── total_score
├── rising_count
├── new_entry_count
├── top_item_ids_json
├── metric_summary_json
├── created_at
└── updated_at
```

### 8.5 用户标注 UserTrendAction

```text
user_trend_action
├── id
├── user_id
├── trend_item_id
├── action_type
├── tags_json
├── note
├── created_at
└── updated_at
```

### 8.6 采集任务 CollectJob

```text
collect_job
├── id
├── source_id
├── trigger_type
├── status
├── started_at
├── finished_at
├── fetched_count
├── inserted_count
├── updated_count
├── duplicated_count
├── error_message
└── created_at
```

## 9. 接口初稿

### 9.1 热点查询

```text
GET /api/global-trend/trends
GET /api/global-trend/trends/{id}
GET /api/global-trend/trends/{id}/snapshots
GET /api/global-trend/trends/search
```

### 9.2 数据分析

```text
GET /api/global-trend/analytics/overview
GET /api/global-trend/analytics/trend-lines
GET /api/global-trend/analytics/platform-comparison
GET /api/global-trend/analytics/topic-distribution
GET /api/global-trend/analytics/ranking-changes
GET /api/global-trend/analytics/heatmap
```

### 9.3 用户操作

```text
POST /api/global-trend/trends/{id}/actions
DELETE /api/global-trend/trends/{id}/actions/{type}
POST /api/global-trend/trends/{id}/convert-topic
```

### 9.4 数据源管理

```text
GET /api/global-trend/sources
PUT /api/global-trend/sources/{code}
POST /api/global-trend/sources/{code}/test
POST /api/global-trend/sources/{code}/collect
```

### 9.5 采集任务

```text
GET /api/global-trend/collect-jobs
GET /api/global-trend/collect-jobs/{id}
```

## 10. 技术和架构建议

按照平台架构规范，后续开发建议新增：

- 前端应用：`frontend/apps/global-trend-monitor-web`。
- 后端模块：`backend/vcoding-global-trend-monitor`。
- 共享认证：复用 `auth-web`、`vcoding-auth` 和 Gateway 登录态。
- 前端技术栈：Vue 3、TypeScript、Vite、Vue Router、Pinia、Axios、Element Plus。
- 后端技术栈：Java 17、Spring Boot 3、MyBatis-Plus、MySQL 8、Redis、Flyway。
- 图表能力：后续技术设计阶段可优先评估 Apache ECharts 等 Vue 生态成熟图表库，并统一封装到业务应用或共享 UI 包中。

后端采集能力建议分层：

```text
api
  ↓
application
  ↓
domain
  ↓
infrastructure
    ├── source/youtube
    ├── source/github
    ├── source/reddit
    ├── source/producthunt
    └── ai-summary
```

关键原则：

- 每个平台一个独立 connector，避免平台差异污染核心业务。
- connector 输出统一的 `TrendItemDraft`，由应用层统一去重、评分和入库。
- API Key、OAuth Token 等密钥只保存在后端，不暴露给前端。
- 采集任务失败要可重试，但不能无限重试。
- 原始响应只保存必要摘要，不长期保存大量无关个人信息。

## 11. 合规和风控边界

本系统必须遵守以下边界：

- 优先使用官方 API、RSS、开放数据集或授权第三方数据源。
- 不绕过登录、验证码、付费墙、机器人检测、签名校验或访问频率限制。
- 不采集非公开内容、私信、封闭社群或需要用户授权但未授权的数据。
- 不把完整原文、完整视频、完整评论区作为默认存储对象。
- 默认只存标题、摘要、链接、公开指标和必要元数据。
- 对平台 API Key、Token、用户授权信息做加密或安全存储。
- 对外展示时保留原文链接和来源平台。
- 对每个平台单独维护接入说明、限流、使用条款和失效风险。

特别说明：

- X、TikTok、Reddit、Product Hunt 等平台的 API 权限、套餐、限流和商业使用条款可能变化，应在开发前再次确认。
- Google Trends 不建议作为一期自动化采集主源，除非采用明确可商用的数据供应商。
- 如果未来需要商业化售卖热点数据，需要额外评估各平台的数据再分发许可。

## 12. 一期非范围

一期不做：

- 自动抓取所有海外网站。
- 绕过反爬的大规模网页爬虫。
- 评论区全文采集和用户画像。
- 自动下载视频、图片或音频素材。
- 自动搬运或发布内容到第三方平台。
- 完整 RBAC 权限体系。
- 多租户计费。
- 实时秒级趋势监控。
- 复杂 AI 趋势预测。

## 13. 验收标准

一期完成时应满足：

- 至少接入 2 个稳定数据源：YouTube 和 GitHub。
- 支持每日定时采集和手动采集。
- 采集任务有成功、失败、入库、去重记录。
- 热点列表可按日期、平台、主题筛选。
- 热点详情可查看原文链接、原始指标、中文摘要和关键词。
- 数据洞察页可展示总热度趋势、平台对比、主题分布、排名变化和单条热点指标走势。
- 图表支持日期范围、平台、主题等基础筛选，并能跳转到对应热点列表或详情。
- 支持收藏、备注和归档。
- 支持基础搜索。
- 数据源配置可以启用、停用和测试连接。
- 所有未登录访问统一跳转到 `auth-web`。
- API Key 和 Token 不出现在前端代码、接口响应和日志中。

## 14. 里程碑建议

### M1：需求和数据源验证

- 确认首批数据源。
- 申请或准备 API Key。
- 为每个数据源写最小可用拉取脚本。
- 验证返回字段、限流、地区和分类能力。

### M2：后端采集闭环

- 新增业务后端模块。
- 建立数据源、热点内容、指标快照和任务日志表。
- 接入首批 2 个 connector。
- 完成去重、评分和入库。

### M3：前端查看闭环

- 新增业务前端应用。
- 完成今日热点、平台榜单、详情页和收藏归档。
- 接入统一登录。

### M4：AI 摘要和主题分类

- 接入中文摘要生成。
- 接入关键词和主题分类。
- 增加批量补摘要任务。

### M5：数据源扩展和稳定性

- 扩展 Reddit、Product Hunt、News 等来源。
- 增加失败告警、配额统计和重试策略。
- 完成一期验收。

## 15. 参考资料

- YouTube Data API `videos.list`：https://developers.google.com/youtube/v3/docs/videos/list
- YouTube Data API quota：https://developers.google.com/youtube/v3/determine_quota_cost
- X API 文档：https://docs.x.com/x-api
- Reddit API 文档：https://www.reddit.com/dev/api/
- Reddit 数据 API 条款：https://www.redditinc.com/policies/data-api-terms
- Hacker News API：https://github.com/HackerNews/API
- GitHub Trending：https://github.com/trending
- GitHub REST Search API：https://docs.github.com/en/rest/search/search
- Product Hunt API v2：https://api.producthunt.com/v2/docs
- Mastodon Trends API：https://docs.joinmastodon.org/methods/trends/
- Bluesky API 文档：https://docs.bsky.app/docs/api
- TikTok Research API：https://developers.tiktok.com/doc/research-api-overview/
- NewsAPI Top headlines：https://newsapi.org/docs/endpoints/top-headlines
- GDELT DOC 2.0 API：https://blog.gdeltproject.org/gdelt-doc-2-0-api-debuts/
