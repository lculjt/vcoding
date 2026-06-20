# Setup Backend Common Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立后端统一响应、错误码、业务异常、全局异常处理和 traceId 基础能力。

**Architecture:** 通用 Web 基础能力放在 `vcoding-common`，通过 `com.vcoding` 包扫描被 `vcoding-auth` 使用。`vcoding-auth` 只新增健康检查接口验证统一响应形态，不实现验证码、注册或登录业务。

**Tech Stack:** Java 17、Spring Boot 3、Spring Web MVC、Maven 多模块、SLF4J MDC。

---

### Task 1: 通用响应与异常模型

**Files:**
- Modify: `backend/vcoding-common/pom.xml`
- Create: `backend/vcoding-common/src/main/java/com/vcoding/common/response/ApiResponse.java`
- Create: `backend/vcoding-common/src/main/java/com/vcoding/common/response/ErrorCode.java`
- Create: `backend/vcoding-common/src/main/java/com/vcoding/common/exception/BusinessException.java`

- [ ] **Step 1: 为 common 增加 Spring Web 依赖**

让 common 可以承载 `RestControllerAdvice`、`Filter`、`ResponseEntity` 等 Web 基础能力。

- [ ] **Step 2: 创建 ApiResponse**

统一返回 `code`、`message`、`data`、`traceId`。

- [ ] **Step 3: 创建 ErrorCode 和 BusinessException**

错误码携带默认消息和 HTTP 状态码，业务异常携带错误码和可选 data。

### Task 2: traceId 与全局异常处理

**Files:**
- Create: `backend/vcoding-common/src/main/java/com/vcoding/common/trace/TraceIdHolder.java`
- Create: `backend/vcoding-common/src/main/java/com/vcoding/common/trace/TraceIdFilter.java`
- Create: `backend/vcoding-common/src/main/java/com/vcoding/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 TraceIdHolder**

基于 MDC 保存当前请求 traceId。

- [ ] **Step 2: 创建 TraceIdFilter**

读取或生成 `X-Trace-Id`，写入响应头和 MDC，请求结束后清理。

- [ ] **Step 3: 创建 GlobalExceptionHandler**

统一处理业务异常、参数校验异常、请求格式异常和未知异常。

### Task 3: vcoding-auth 接入验证

**Files:**
- Modify: `backend/vcoding-auth/src/main/java/com/vcoding/auth/VcodingAuthApplication.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/api/HealthController.java`

- [ ] **Step 1: 扩大 Spring 扫描范围**

让 `vcoding-auth` 扫描 `com.vcoding.common` 下的通用组件。

- [ ] **Step 2: 添加健康检查接口**

新增 `GET /api/auth/health`，返回统一响应结构。

### Task 4: 验证

**Files:**
- Modify: `scripts/verify-structure.sh`

- [ ] **Step 1: 更新结构验证脚本**

检查 common 基础类和健康检查接口存在。

- [ ] **Step 2: 运行验证**

Run:

```bash
bash scripts/verify-structure.sh
mvn -f backend/pom.xml validate
mvn -f backend/pom.xml compile
```

Expected: 全部成功。
