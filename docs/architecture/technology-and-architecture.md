# 技术栈与架构规范

## 目标

本文用于固定 vcoding 平台的前后端技术栈、模块边界、依赖同步方式和后续系统扩展规则。后续实现统一登录、门户和业务系统时，应优先遵守本文。

当前阶段先确立架构约定，不要求一次性引入所有依赖。实际依赖应随着对应功能落地逐步加入。

## 总体架构

vcoding 是“统一用户中心 + 多业务系统”的平台型项目。所有业务系统共用统一账号体系，只有一个登录入口。

```text
auth-web
  ↓
vcoding-auth
  ↓
portal-web
  ↓
业务系统 xxx-web / vcoding-xxx
```

- `auth-web`：唯一登录前端。
- `vcoding-auth`：统一用户中心后端。
- `portal-web`：登录后的系统入口。
- `xxx-web`：具体业务系统前端。
- `vcoding-xxx`：具体业务系统后端模块。

## 前端技术栈

### 核心选型

- 框架：Vue 3
- 语言：TypeScript
- 构建工具：Vite
- 包管理：pnpm workspace
- 路由：Vue Router
- 状态管理：Pinia
- 请求库：Axios
- UI 组件库：Element Plus
- 代码规范：ESLint + Prettier
- 测试：第一阶段以人工验收为主，不强制自动化测试

### 前端目录约定

```text
frontend/
├── apps/
│   ├── auth-web/
│   ├── portal-web/
│   └── <system-name>-web/
└── packages/
    ├── ui/
    ├── auth-client/
    ├── api-client/
    ├── shared/
    ├── eslint-config/
    └── tsconfig/
```

说明：

- `apps/*` 只放可独立运行的应用。
- `packages/ui` 放平台通用 UI 组件和二次封装。
- `packages/auth-client` 放登录状态、当前用户、登录跳转和退出登录能力。JWT 存在 HttpOnly Cookie 中，前端不读取 Token 明文。
- `packages/api-client` 放由 OpenAPI 生成或维护的前端请求客户端。
- `packages/shared` 放通用类型、常量和纯工具函数。
- `packages/eslint-config` 放前端统一 ESLint 配置。
- `packages/tsconfig` 放前端统一 TypeScript 配置。

当前仓库已有 `ui`、`auth-client`、`shared`。`api-client`、`eslint-config`、`tsconfig` 可在接入真实 Vue 应用和接口生成时新增。

### 路由规范

每个前端应用使用 Vue Router。路由配置按应用内职责拆分：

```text
src/router/
├── index.ts
├── guards.ts
└── routes.ts
```

- `index.ts` 创建路由实例。
- `guards.ts` 定义登录校验、标题处理和权限前置判断。
- `routes.ts` 定义静态路由。

认证跳转规则：

- `auth-web` 不依赖业务系统路由，只负责登录和回跳。
- `portal-web` 和业务系统进入前必须检查登录态。
- 未登录时统一跳转到 `auth-web`。
- 跳转登录时携带 `redirect` 参数，登录成功后回到原目标地址。

本地开发时，各前端应用通过 Vite proxy 将 `/api` 代理到后端，尽量模拟同源请求，降低 HttpOnly Cookie 与 CORS 调试成本。

```ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 状态管理规范

统一使用 Pinia。状态按职责拆分：

```text
src/stores/
├── auth.ts
├── user.ts
└── app.ts
```

- `auth`：Token、登录状态、登录和退出动作。
- 使用 HttpOnly Cookie 时，前端不读取 JWT 明文；`auth` 只保存登录状态和当前会话信息。
- `user`：当前用户、角色、权限和用户偏好。
- `app`：应用级 UI 状态，例如侧边栏、主题、布局模式。

跨应用共享的认证状态读写逻辑，不直接写在各应用内，应沉淀到 `packages/auth-client`。

### 请求层规范

每个应用不直接散落调用 Axios。请求应通过统一封装：

```text
src/api/
├── request.ts
└── modules/
```

通用能力应放到 `packages/api-client`：

- 统一 baseURL。
- 统一请求头。
- 统一 `withCredentials` 配置。
- 统一 401 处理。
- 统一错误模型。
- 统一接口类型。

由于第一阶段采用 JWT + HttpOnly Cookie，前端请求封装必须开启凭证携带：

```ts
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  withCredentials: true
})
```

### 样式规范

前端样式使用 SCSS + CSS Variables。

- 全局样式放在各应用 `src/styles` 或沉淀到 `packages/ui`。
- 主题变量优先使用 CSS Variables。
- 复杂组件样式使用 SCSS。
- 单文件组件样式优先使用 `scoped`。
- 第一阶段不引入 Tailwind CSS 或 UnoCSS。

### 前端依赖同步

前端依赖版本由 `frontend/package.json` 和 pnpm workspace 统一管理。

推荐规则：

- 所有应用共享同一套核心依赖版本。
- Vue、Vite、TypeScript、Vue Router、Pinia、Element Plus、Axios、ESLint、Prettier 不在各应用中随意分叉版本。
- 内部包使用 `workspace:*`。
- 后续可使用 pnpm catalog 统一声明公共依赖版本。

### 前端代码规范

第一阶段前端启用 ESLint + Prettier，后端暂不强制 Checkstyle 或 Spotless。

- 前端 lint 和 format 配置应在 workspace 内统一维护。
- 后续可新增 `frontend/packages/eslint-config` 统一复用规则。
- 后续可新增 `frontend/packages/tsconfig` 统一复用 TypeScript 配置。
- 后端先使用 IDE 默认格式化，并以 Maven validate 作为最低验证。

## 后端技术栈

### 核心选型

- 语言：Java 17
- 框架：Spring Boot 3
- 构建：Maven 多模块
- Web：Spring Web MVC
- 安全：Spring Security
- ORM：MyBatis-Plus
- 数据库：MySQL 8
- 缓存：Redis
- 数据库迁移：Flyway
- 参数校验：Hibernate Validator
- API 文档：springdoc-openapi
- 日志：SLF4J + Logback
- 代码简化：Lombok
- 测试：第一阶段以人工验收为主，不强制自动化测试

### 后端模块约定

```text
backend/
├── pom.xml
├── vcoding-common/
├── vcoding-auth/
├── vcoding-gateway/
└── vcoding-<system-name>/
```

- `vcoding-common`：通用返回结构、异常、基础工具、公共常量。
- `vcoding-auth`：用户、账号密码登录、Token、当前用户、角色权限。
- `vcoding-gateway`：统一入口边界，后续可演进为真实网关。
- `vcoding-<system-name>`：业务系统后端模块。

### 后端包结构规范

每个业务模块内部推荐使用以下结构：

```text
src/main/java/com/vcoding/<module>/
├── api/
├── application/
├── domain/
├── infrastructure/
└── config/
```

- `api`：Controller、请求对象、响应对象。
- `application`：应用服务，编排业务流程。
- `domain`：领域模型、领域规则和核心业务接口。
- `infrastructure`：数据库、外部接口、缓存、文件等技术实现。
- `config`：模块内配置。

简单模块可以先按 `controller`、`service`、`mapper`、`entity` 落地，但一旦业务复杂，应逐步向上面的结构收敛。

### ORM 规范

后端统一使用 MyBatis-Plus。

推荐约定：

- 表实体放在 `infrastructure/persistence/entity`。
- Mapper 放在 `infrastructure/persistence/mapper`。
- XML SQL 放在 `src/main/resources/mapper`。
- 不在 Controller 中直接调用 Mapper。
- 查询条件复杂时优先封装到应用服务或仓储实现中。
- 分页、逻辑删除、自动填充等 MyBatis-Plus 能力统一在公共配置中处理。

### 数据库迁移规范

数据库结构变更使用 Flyway 管理。

迁移文件推荐放在各模块：

```text
src/main/resources/db/migration/
```

命名规则：

```text
V版本号__中文或英文描述.sql
```

示例：

```text
V1__create_user_tables.sql
V2__add_user_status.sql
```

数据库表结构不得只靠手工修改，必须有迁移脚本。

### Redis 使用规范

第一阶段引入 Redis，主要用于验证码和防刷控制。

第一阶段 Redis 用途：

- 图形验证码。
- 短信验证码。
- 短信发送冷却。
- 单手机号发送次数限制。
- 单 IP 发送次数限制。
- 验证码校验失败次数限制。

第一阶段不使用 Redis 做 JWT 会话存储。JWT 仍保持无状态，后续如果需要强制下线、Token 黑名单或多端登录控制，再扩展 Redis 用途。

### 后端依赖同步

后端依赖版本由 `backend/pom.xml` 统一管理。

推荐规则：

- 使用 Spring Boot BOM 管理 Spring 生态依赖版本。
- 第三方依赖版本集中放在父 `pom.xml` 的 `dependencyManagement`。
- 子模块只声明依赖，不单独声明版本，除非确有隔离理由。
- 内部模块版本统一使用 `${project.version}`。
- Lombok 版本由父 `pom.xml` 统一管理，使用模块按需以 `provided` 方式引入。

后续如果模块数量明显增加，可以新增内部 BOM 模块，但当前阶段父 `pom.xml` 足够。

## 前后端接口同步

前后端接口同步使用 OpenAPI + openapi-typescript + 自封装 Axios client。

长期规则：

- 后端通过 `springdoc-openapi` 暴露接口文档。
- 前端通过 `openapi-typescript` 生成 TypeScript 类型。
- 生成结果放入 `frontend/packages/api-client`。
- Axios 请求实例和业务 API 方法由 `frontend/packages/api-client` 手写封装。
- 应用层通过 `api-client` 调用接口，不手写重复类型，不在应用内散落 Axios 调用。

推荐目录：

```text
frontend/packages/api-client/
└── src/
    ├── generated/
    │   └── schema.ts
    ├── request.ts
    ├── modules/
    │   ├── auth.ts
    │   └── user.ts
    └── index.ts
```

接口变更流程：

1. 后端修改接口定义。
2. 更新 OpenAPI 文档。
3. 重新生成 `api-client` 中的 OpenAPI 类型。
4. 前端按类型错误修正调用方。

这样可以避免前后端字段名、枚举、请求参数和响应结构长期不同步。

## 认证与权限演进

第一阶段实现带注册、验证码和管理员创建账号能力的认证闭环：

- 用户名 + 密码登录。
- 手机号 + 密码登录。
- 手机号 + 短信验证码登录。
- 用户自助注册。
- 内置超级管理员账号。
- 超级管理员创建普通用户账号。
- JWT + HttpOnly Cookie 登录态。
- 获取当前用户。
- 退出登录。
- 未登录跳转统一登录页。
- 图形验证码。
- 短信验证码。

第一阶段权限模型只做登录态和最小管理员身份，不做完整 RBAC。

- 普通用户登录后可进入门户和示例业务系统。
- 超级管理员可进入用户管理页面并创建账号。
- 不做角色、权限点、菜单权限、系统入口权限和数据权限。

### 账号体系

第一阶段支持三种登录方式：

```text
USERNAME_PASSWORD
PHONE_PASSWORD
PHONE_SMS
```

前端登录页可简化成两个入口：

- 密码登录：输入用户名或手机号 + 密码。
- 短信登录：输入手机号 + 短信验证码。

自助注册使用：

```text
手机号 + 短信验证码 + 密码 + 可选用户名 + 可选昵称
```

用户唯一性约束：

- `phone` 必填且唯一。
- `username` 唯一；用户未填写时可由系统生成。

最小用户表建议：

```text
sys_user
├── id
├── username
├── phone
├── password_hash
├── nickname
├── user_type       # ADMIN / USER
├── status          # ENABLED / DISABLED
├── created_at
└── updated_at
```

### JWT 与 Cookie

第一阶段认证方式为 JWT + HttpOnly Cookie。

登录成功后，后端通过 `Set-Cookie` 写入 JWT，前端 JavaScript 不读取 Token 明文。

推荐 Cookie：

```text
Name: VCODING_TOKEN
HttpOnly: true
Secure: prod 为 true，local/dev 可为 false
SameSite: Lax
Path: /
Max-Age: 与 JWT 过期时间一致
```

后端认证过滤器从 Cookie 中读取 `VCODING_TOKEN`，校验 JWT 后写入当前认证上下文。退出登录时由后端清除 Cookie。

使用 Cookie 后需要关注 CSRF。第一阶段使用 `SameSite=Lax`，并要求修改类接口只接受 JSON 请求。后续如出现跨主域部署，再补充 CSRF Token、CORS credentials 白名单和更严格的 Cookie 策略。

### 统一认证与业务系统鉴权

所有业务系统必须复用统一用户中心，不单独实现账号密码登录、手机号验证码登录或独立用户表。

推荐请求链路：

```text
浏览器
  ↓
auth-web 统一登录
  ↓
vcoding-auth 签发 JWT HttpOnly Cookie
  ↓
portal-web 或业务系统前端
  ↓
vcoding-gateway 或公共鉴权过滤器校验登录态
  ↓
vcoding-<system-name> 处理业务接口
```

统一认证层负责判断“用户是谁、是否已登录、账号是否可用”：

- 从 `VCODING_TOKEN` Cookie 读取 JWT。
- 校验 JWT 签名和过期时间。
- 解析 `userId` 等登录身份信息。
- 必要时查询用户状态，确认用户未被禁用。
- 将当前用户写入请求上下文，供业务模块读取。

业务系统只负责判断“当前用户能不能做当前业务操作”：

- 普通接口只要求已登录。
- 管理接口第一阶段可先要求 `adminFlag = true`。
- 资源归属类接口应校验数据是否属于当前用户或当前用户是否有管理权限。
- 未登录统一返回未登录错误，已登录但无权限统一返回无权限错误。

推荐公共能力沉淀位置：

```text
backend/vcoding-common
├── auth/
│   ├── CurrentUser
│   ├── AuthContext
│   └── AuthRequiredFilter
└── response/
    └── ErrorCode

backend/vcoding-gateway
└── 统一入口鉴权和路由转发
```

第一阶段可以先在公共过滤器中完成登录态校验，再逐步演进到网关统一处理。无论采用哪种实现，业务模块都不直接解析 Cookie，不直接校验 JWT 签名，而是通过公共上下文读取当前用户。

### 验证码

第一阶段包含图形验证码和短信验证码。

推荐流程：

```text
获取图形验证码
  ↓
提交手机号 + 图形验证码，请求发送短信验证码
  ↓
后端校验图形验证码
  ↓
生成 6 位短信验证码
  ↓
验证码 hash 后写入 Redis
  ↓
调用短信发送器
  ↓
用户提交短信验证码完成注册、登录或找回密码
```

短信验证码规则：

- 长度：6 位数字。
- 有效期：5 分钟。
- 发送冷却：60 秒。
- 校验失败超过限制后验证码失效。
- 校验成功后立即删除。
- Redis 中不存明文验证码，存 hash。

Redis Key 示例：

```text
captcha:image:{captchaId}
captcha:sms:{scene}:{phone}
captcha:sms:cooldown:{scene}:{phone}
captcha:sms:limit:phone:{scene}:{phone}:{yyyyMMdd}
captcha:sms:limit:ip:{scene}:{ip}:{yyyyMMdd}
captcha:sms:attempt:{scene}:{phone}
```

短信场景：

```text
register
login
reset-password
bind-phone
change-phone
```

短信发送采用接口抽象：

```text
SmsSender
├── ConsoleSmsSender
└── ProviderSmsSender
```

- `ConsoleSmsSender` 用于本地开发，将验证码输出到日志。
- `ProviderSmsSender` 用于生产环境，后续接阿里云、腾讯云或其他短信服务商。
- 验证码生成、Redis 存储、频率限制和校验逻辑归 `SmsCodeService`，短信平台调用只归 `SmsSender`。

### 统一响应与异常处理

后端接口统一响应结构：

```json
{
  "code": "SUCCESS",
  "message": "成功",
  "data": {},
  "traceId": "..."
}
```

成功响应：

- `code` 为 `SUCCESS`。
- `message` 为 `成功`。
- `data` 为实际数据。
- `traceId` 为当前请求追踪 ID。

失败响应：

- `code` 为业务错误码。
- `message` 为可读错误信息。
- `data` 为 `null` 或错误详情。
- `traceId` 为当前请求追踪 ID。

异常处理统一使用 `GlobalExceptionHandler`。Controller 不直接捕获业务异常，不直接返回 `Map`，统一返回 `ApiResponse<T>`。

`vcoding-common` 推荐承载：

```text
response/
├── ApiResponse
└── ErrorCode
exception/
├── BusinessException
└── GlobalExceptionHandler
trace/
├── TraceIdFilter
└── TraceIdHolder
```

第一阶段错误码至少包含：

```text
SUCCESS
COMMON_BAD_REQUEST
COMMON_UNAUTHORIZED
COMMON_FORBIDDEN
COMMON_NOT_FOUND
COMMON_INTERNAL_ERROR
AUTH_INVALID_USERNAME_OR_PASSWORD
AUTH_INVALID_SMS_CODE
AUTH_SMS_CODE_EXPIRED
AUTH_USER_DISABLED
AUTH_NOT_LOGIN
```

### traceId

第一阶段加入 `traceId`。

- 请求头使用 `X-Trace-Id`。
- 如果请求带 `X-Trace-Id`，后端沿用。
- 如果请求未带 `X-Trace-Id`，后端生成。
- 日志 MDC 写入 `traceId`。
- 成功和失败响应都返回 `traceId`。

推荐日志格式包含：

```text
[%X{traceId}] %-5level %logger - %msg%n
```

第二阶段再扩展：

- 角色。
- 权限点。
- 菜单权限。
- 系统入口权限。

第三阶段再考虑：

- 单点登录协议。
- 多端登录控制。
- 刷新 Token。
- 审计日志。
- Spring Cloud Gateway 或其他真实网关方案。

## 环境与本地开发

### 环境划分

后端环境：

```text
application.yml
application-local.yml
application-dev.yml
application-prod.yml
```

前端环境：

```text
.env.local
.env.development
.env.production
```

环境含义：

- `local`：本地开发。
- `dev`：开发或测试服务器。
- `prod`：生产环境。

配置原则：

- 公共默认配置放在 `application.yml`。
- 本地 MySQL、Redis、API 文档开关放在 `application-local.yml`。
- 敏感信息不提交明文，使用环境变量。
- 生产环境 JWT 密钥、数据库密码、Redis 密码、短信服务密钥必须来自环境变量。
- 生产环境 Cookie `Secure=true`。
- 生产日志不得打印完整验证码、密码、Token 或短信服务密钥。

### 本地 Docker Compose

第一阶段提供本地 Docker Compose，用于启动 MySQL 和 Redis。

推荐位置：

```text
deploy/local/docker-compose.yml
```

服务：

```text
mysql: MySQL 8
redis: Redis 7
```

推荐启动命令：

```bash
docker compose -f deploy/local/docker-compose.yml up -d
```

本地默认连接：

```text
MySQL: localhost:3306 / database vcoding / user vcoding
Redis: localhost:6379
```

### 验证策略

第一阶段以人工验收为主，不强制 Vitest、Playwright 或后端单元测试。

提交前至少运行：

```bash
bash scripts/verify-structure.sh
mvn -f backend/pom.xml validate
pnpm -C frontend install
pnpm -C frontend build
```

## 不立即引入的能力

以下能力暂不作为第一阶段必选项：

- 微服务拆分。
- Spring Cloud 全量体系。
- 多租户。
- 复杂工作流。
- 分布式事务。
- OAuth2 授权服务器。
- 前端微前端框架。
- Refresh Token。
- Token 黑名单。
- 多端登录控制。
- 完整 RBAC。
- 数据权限。
- 自动化测试强制门禁。

这些能力应在真实需求出现后再进入设计，不提前堆技术。

## 新增系统规范

新增业务系统时，至少补齐：

```text
frontend/apps/<system-name>-web
backend/vcoding-<system-name>
```

并遵守：

- 前端复用 `auth-client`、`api-client`、`ui` 和 `shared`。
- 后端复用 `vcoding-common` 和 `vcoding-auth` 的认证上下文。
- 不新增独立登录页。
- 不绕过统一用户中心。
- 需要新依赖时，优先在根级依赖管理中统一声明版本。

## 推荐第一阶段落地顺序

1. 将前端骨架升级为 Vue 3 + Vite + TypeScript。
2. 补齐 `ui`、`auth-client`、`shared` 的基础导出结构。
3. 将后端父工程升级为 Spring Boot 3 父子模块依赖管理。
4. 提供本地 MySQL + Redis 的 Docker Compose。
5. 在 `vcoding-auth` 中接入 Spring Web、Spring Security、MyBatis-Plus、Flyway 和 Redis。
6. 实现统一响应、全局异常处理和 traceId。
7. 实现图形验证码、短信验证码和短信发送器抽象。
8. 实现自助注册、管理员创建账号、三种登录方式和 JWT HttpOnly Cookie。
9. 通过 OpenAPI 生成类型并封装 `api-client`，让 `auth-web` 和 `portal-web` 调用真实接口。
