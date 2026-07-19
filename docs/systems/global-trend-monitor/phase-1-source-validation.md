# 海外热点聚合观察台一期数据源验证记录

## 1. 验证范围

本文记录一期首批数据源的最小接口验证结果。验证接口只读取少量公开数据，返回字段、数量、认证状态和限流摘要，不写入 `gtm_trend_item`、`gtm_metric_snapshot` 或采集任务表。

验证时间：2026-07-19

验证模块：`backend/vcoding-global-trend-monitor`

验证接口：`POST /api/global-trend/sources/{code}/test`

权限要求：管理员登录态。接口内部仍经过 Gateway 用户头校验。

## 2. 官方接口和字段口径

| 数据源 | 验证接口 | 认证 | 主要字段 |
| --- | --- | --- | --- |
| Hacker News | `GET /v0/topstories.json` + `GET /v0/item/{id}.json` | 无需认证 | `id`、`type`、`by`、`time`、`title`、`url`、`score`、`descendants` |
| GitHub | `GET /search/repositories` | 可匿名，也支持 Bearer Token | `id`、`full_name`、`html_url`、`description`、`language`、`stargazers_count`、`forks_count`、`created_at`、`updated_at` |
| YouTube | `GET /youtube/v3/videos` | API Key | `id`、`snippet.title`、`snippet.channelId`、`snippet.publishedAt`、`snippet.categoryId`、`statistics.viewCount`、`statistics.likeCount`、`statistics.commentCount` |

官方资料：

- [Hacker News API](https://github.com/HackerNews/API)
- [GitHub REST API rate limits](https://docs.github.com/en/rest/rate-limit/rate-limit)
- [YouTube videos.list](https://developers.google.com/youtube/v3/docs/videos/list)
- [YouTube videos implementation](https://developers.google.com/youtube/v3/guides/implementation/videos)

## 3. 实现方式

后端新增统一 `SourceValidator` 接口和三个验证器：

```text
SourceValidationService
  ├── HackerNewsSourceValidator
  ├── GitHubSourceValidator
  └── YouTubeSourceValidator
```

统一返回：

```json
{
  "sourceCode": "github",
  "success": true,
  "statusCode": 200,
  "itemCount": 5,
  "durationMillis": 2293,
  "authentication": "NONE",
  "rateLimit": "remaining=9,limit=10,resetEpoch=...",
  "observedFields": [],
  "missingFields": [],
  "message": "验证成功"
}
```

安全约束：

- YouTube API Key 只从 `VCODING_YOUTUBE_API_KEY` 环境变量读取。
- GitHub Token 只从 `VCODING_GITHUB_TOKEN` 环境变量读取。
- 错误响应不返回第三方原始响应，不返回包含 Key 的请求 URL。
- 日志只记录数据源、成功状态、HTTP 状态、数量和耗时。
- 验证器最多读取少量样本：Hacker News 和 GitHub 默认 5 条，YouTube 默认 5 条。

## 4. 实测结果

### Hacker News

- 结果：通过。
- HTTP 状态：`200`。
- 样本数量：`5`。
- 认证：`NONE`。
- 字段：需求字段全部出现。
- 限流：接口文档未声明固定限流值，当前结果记录为 `UNREPORTED`。
- 结论：可以进入 connector 正式开发，采用“先取 ID 列表，再按 ID 读取 item”的两步模式。

### GitHub

- 结果：通过。
- HTTP 状态：`200`。
- 样本数量：`5`。
- 认证：当前使用匿名请求。
- 字段：需求字段全部出现。
- 实测限流摘要：`remaining=9, limit=10`。
- 结论：一期正式采集应优先配置 GitHub Token，避免匿名搜索配额过低；请求必须读取 `X-RateLimit-*` 响应头，并在达到限制后停止重试。

### YouTube

- 结果：等待配置 API Key。
- 当前行为：未配置 Key 时不发起外部请求，返回 `API_KEY_MISSING`。
- 预期请求参数：`part=snippet,statistics,contentDetails`、`chart=mostPopular`、`regionCode=US`、`maxResults=5`。
- 官方接口单次 `videos.list` 调用配额成本为 1；正式采集前需要配置 Key 并确认项目配额。
- 结论：验证代码已完成，真实字段验证待补充 `VCODING_YOUTUBE_API_KEY` 后执行。

## 5. 本地验证命令

启动热点后端：

```bash
mvn -f backend/vcoding-global-trend-monitor/pom.xml spring-boot:run
```

然后通过管理员登录态调用：

```text
POST http://localhost:8084/api/global-trend/sources/hacker-news/test
POST http://localhost:8084/api/global-trend/sources/github/test
POST http://localhost:8084/api/global-trend/sources/youtube/test
```

建议配置：

```bash
export VCODING_GITHUB_TOKEN="..."
export VCODING_YOUTUBE_API_KEY="..."
```

不要把真实 Key 写入仓库文件、Flyway 数据表、接口响应或日志。

## 6. 下一步结论

- Hacker News：可以进入正式 connector 开发。
- GitHub：可以进入正式 connector 开发，但正式环境应使用 Token 并实现限流保护。
- YouTube：需要先配置 API Key 完成一次真实调用，再进入正式 connector 开发。
- 三个平台正式 connector 都应输出统一的 `TrendItemDraft`，验证器不负责热点入库和热度评分。
