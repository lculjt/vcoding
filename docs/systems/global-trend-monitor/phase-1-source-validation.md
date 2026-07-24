# 海外热点聚合观察台一期数据源验证记录

## 1. 验证范围

本文记录数据源的最小接口验证结果。当前一期正式范围只保留 YouTube 和 GitHub；Hacker News 的验证结论作为二期预研材料保留，不再计入一期验收和运行范围。

验证时间：2026-07-19

验证模块：`backend/vcoding-global-trend-monitor`

验证接口：`POST /api/global-trend/sources/{code}/test`

权限要求：管理员登录态。接口内部仍经过 Gateway 用户头校验。

## 2. 官方接口和字段口径

| 数据源 | 验证接口 | 认证 | 主要字段 |
| --- | --- | --- | --- |
| GitHub | `GET /trending/{language}?since={daily|weekly|monthly}&spoken_language_code={code}` | 无需认证 | 仓库名、描述、编程语言、总 Star、Fork、周期新增 Star |
| YouTube | `GET /youtube/v3/videos` | API Key | `id`、`snippet.title`、`snippet.channelId`、`snippet.publishedAt`、`snippet.categoryId`、`statistics.viewCount`、`statistics.likeCount`、`statistics.commentCount` |

官方资料：

- [GitHub Trending](https://github.com/trending)
- [GitHub REST API rate limits](https://docs.github.com/en/rest/rate-limit/rate-limit)
- [YouTube videos.list](https://developers.google.com/youtube/v3/docs/videos/list)
- [YouTube videos implementation](https://developers.google.com/youtube/v3/guides/implementation/videos)

## 3. 实现方式

后端新增统一 `SourceValidator` 接口和两个一期验证器：

```text
SourceValidationService
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
- GitHub Trending HTML 当前无需 Token；采集条件只从本地或部署环境变量读取。
- 错误响应不返回第三方原始响应，不返回包含 Key 的请求 URL。
- 日志只记录数据源、成功状态、HTTP 状态、数量和耗时。
- 验证器最多读取少量样本：GitHub 默认 5 条，YouTube 默认 5 条。

## 4. 实测结果

### Hacker News（二期预研记录）

- 结果：通过。
- HTTP 状态：`200`。
- 样本数量：`5`。
- 认证：`NONE`。
- 字段：需求字段全部出现。
- 限流：接口文档未声明固定限流值，当前结果记录为 `UNREPORTED`。
- 结论：接口可用，但已从一期正式范围移出；二期恢复时可采用“先取 ID 列表，再按 ID 读取 item”的两步模式。

### GitHub

- 结果：通过。
- HTTP 状态：`200`。
- 样本数量：`5`。
- 认证：无需认证。
- 字段：需求字段全部出现。
- 限流：官网 HTML 页面没有 REST API 的 `X-RateLimit-*` 响应头，一期必须低频采集，不做高频网页抓取。
- 结论：一期正式采集解析 GitHub 官网 Trending HTML，支持编程语言、自然语言和日/周/月范围筛选；页面结构变化时需要更新解析器。

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
POST http://localhost:8084/api/global-trend/sources/github/test
POST http://localhost:8084/api/global-trend/sources/youtube/test
```

建议配置：

```bash
export VCODING_GITHUB_TRENDING_LANGUAGE=""
export VCODING_GITHUB_TRENDING_SPOKEN_LANGUAGE_CODE=""
export VCODING_GITHUB_TRENDING_SINCE="daily"
export VCODING_YOUTUBE_API_KEY="..."
```

不要把真实 Key 写入仓库文件、Flyway 数据表、接口响应或日志。

## 6. 下一步结论

- GitHub：可以进入正式 connector 开发，但正式环境应低频解析官网 Trending HTML，并记录页面结构变化风险。
- YouTube：需要先配置 API Key 完成一次真实调用，再进入正式 connector 开发。
- Hacker News：作为二期预研记录保留，一期不提供正式 connector 和验证入口。
- 两个一期正式 connector 都应输出统一的 `TrendItemDraft`，验证器不负责热点入库和热度评分。
