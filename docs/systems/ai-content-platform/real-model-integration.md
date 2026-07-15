# 真实大模型接入说明

本文说明 `vcoding-content` 如何从本地 mock 生成切换到真实大模型调用。

当前实现采用 OpenAI 兼容的 Chat Completions 接口，默认仍为 `mock` 模式，避免没有 API Key 时影响本地选题、草稿等功能。

## 配置方式

启动 `vcoding-content` 前设置环境变量：

```bash
export VCODING_CONTENT_AI_MODE=openai-compatible
export VCODING_CONTENT_AI_API_KEY=你的真实 API Key
export VCODING_CONTENT_AI_BASE_URL=https://api.openai.com/v1
export VCODING_CONTENT_AI_MODEL=gpt-4o-mini
```

可选配置：

```bash
export VCODING_CONTENT_AI_ENDPOINT_PATH=/chat/completions
export VCODING_CONTENT_AI_TIMEOUT_SECONDS=60
export VCODING_CONTENT_AI_TEMPERATURE=0.7
export VCODING_CONTENT_AI_JSON_MODE=true
export VCODING_CONTENT_AI_MAX_TOKENS=4096
```

说明：

- `VCODING_CONTENT_AI_MODE=mock`：使用本地模板生成，不调用外部模型。
- `VCODING_CONTENT_AI_MODE=openai-compatible`：调用真实 OpenAI 兼容接口。
- `VCODING_CONTENT_AI_API_KEY` 不允许提交到仓库，只能通过本地环境变量、部署平台密钥或容器 Secret 注入。
- 如果使用其他 OpenAI 兼容供应商，通常只需要替换 `BASE_URL`、`MODEL` 和 `API_KEY`。
- 如果供应商不支持 `response_format: { type: "json_object" }`，可以设置 `VCODING_CONTENT_AI_JSON_MODE=false`，但模型仍必须按 Prompt 返回 JSON 对象。

## 本地启动示例

```bash
cd backend

export VCODING_CONTENT_AI_MODE=openai-compatible
export VCODING_CONTENT_AI_API_KEY=你的真实 API Key
export VCODING_CONTENT_AI_BASE_URL=https://api.openai.com/v1
export VCODING_CONTENT_AI_MODEL=gpt-4o-mini

mvn -pl vcoding-content -am spring-boot:run
```

如果同时通过网关访问，还需要启动 `vcoding-gateway`，前端继续请求 `/api/content/**`。

## 调用链路

```text
content-web
  -> /api/content/topics/{topicId}/generate
  -> vcoding-gateway
  -> vcoding-content
  -> OpenAI 兼容 Chat Completions API
  -> ai_generation_run 记录结果
  -> ARTICLE / VIDEO_SCRIPT 自动生成草稿
```

## 返回格式要求

后端会要求模型只返回 JSON 对象。不同任务使用不同字段：

- 文章：`title`、`summary`、`body`、`coverPrompt`
- 短视频脚本：`title`、`scriptContent`、`coverPrompt`
- 大纲：`title`、`outline`
- 标题候选：`titleCandidates`
- 摘要：`summary`
- 标签：`tags`
- 封面提示词：`coverPrompt`

后端会兼容模型返回 Markdown 代码块包裹的 JSON，也会把数组字段转成换行字符串保存。

## 当前边界

- 当前调用是同步请求，适合第一阶段验证，不适合长时间生成任务。
- 暂未做模型用量统计、限流、成本记录和供应商级熔断。
- 后续当 Agent 工作台或其他系统也需要模型能力时，应把该实现迁移到共享 `vcoding-ai` 模块。
