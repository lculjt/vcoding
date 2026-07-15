# vcoding

vcoding 是一个面向多业务系统的平台型项目。项目计划支持多个系统共存，但所有系统共用统一用户中心，并通过唯一登录界面完成账号密码登录或手机号验证码登录。用户登录成功后，回到原目标系统；没有目标系统时进入默认业务系统。

当前仓库已建立前后端工程骨架，并在 `vcoding-auth` 中落地统一用户中心的第一批接口能力，包括图形验证码、短信验证码、自助注册、登录、当前用户和退出登录。同时，`vcoding-gateway` 已接入 Spring Cloud Gateway，作为业务接口统一入口。

## 项目结构

```text
vcoding/
├── frontend/
│   ├── apps/
│   │   ├── auth-web/
│   │   ├── portal-web/
│   │   └── demo-system-web/
│   ├── packages/
│   │   ├── ui/
│   │   ├── auth-client/
│   │   └── shared/
│   ├── package.json
│   └── pnpm-workspace.yaml
├── backend/
│   ├── pom.xml
│   ├── vcoding-common/
│   ├── vcoding-auth/
│   ├── vcoding-gateway/
│   └── vcoding-system-demo/
├── docs/
├── scripts/
├── AGENTS.md
└── README.md
```

## 前端

前端位于 `frontend/`，采用 pnpm workspace 的 monorepo 模式。

### 应用

- `frontend/apps/auth-web`：唯一登录界面。所有需要登录的系统在未认证时都应跳转到这里。
- `frontend/apps/portal-web`：可选的登录后系统入口或工作台。
- `frontend/apps/demo-system-web`：业务系统前端示例，用于说明后续系统扩展方式。

### 共享包

- `frontend/packages/ui`：跨系统复用的通用 UI 组件。
- `frontend/packages/auth-client`：前端认证客户端封装，后续承载登录态、Token、用户信息、跳转参数和权限判断。
- `frontend/packages/shared`：跨系统复用的通用类型、常量和工具函数。

## 后端

后端位于 `backend/`，采用 Maven 多模块模式。

- `backend/pom.xml`：Maven 父模块，统一声明子模块和基础构建属性。
- `backend/vcoding-common`：通用基础能力模块。
- `backend/vcoding-auth`：统一用户中心和认证模块。
- `backend/vcoding-gateway`：统一入口、路由和鉴权边界模块。
- `backend/vcoding-system-demo`：业务系统后端示例模块。

## 统一登录与认证接口

当前统一登录边界如下：

1. 用户访问某个需要登录的业务系统。
2. 前端应用或网关发现用户未登录。
3. 用户跳转到 `auth-web`。
4. `auth-web` 调用 `vcoding-auth` 完成账号密码登录或手机号验证码登录。
5. 登录成功后，用户回到原业务系统；没有 `redirect` 时进入默认业务系统。
6. 后端通过统一登录态识别当前用户，业务系统只处理自身业务权限。

已落地的认证相关接口：

```text
GET  /api/auth/captcha/image
POST /api/auth/sms/send
POST /api/auth/register
POST /api/auth/login
POST /api/auth/login/sms
GET  /api/auth/me
POST /api/auth/logout
GET  /api/auth/password/public-key
```

登录成功后，`vcoding-auth` 通过 `Set-Cookie` 写入 `VCODING_TOKEN`。该 Cookie 使用 HttpOnly 模式，前端不读取 JWT 明文，后续请求通过浏览器自动携带登录态。

接口文档由 springdoc-openapi 自动生成，本地启动 `vcoding-auth` 后可访问：

```text
Swagger UI: http://localhost:8081/swagger-ui/index.html
OpenAPI JSON: http://localhost:8081/v3/api-docs
```

业务系统接口采用 gateway 和服务内部双层校验：

```text
浏览器 -> vcoding-gateway:8080 -> 业务服务内部二次校验 -> 业务接口
```

`vcoding-gateway` 校验 `VCODING_TOKEN` 后，会向下游服务追加签名后的内部用户头。业务服务只信任 gateway 签名头，不直接解析 Cookie 或 JWT。

## 开发命令

### 本地依赖

第一阶段本地依赖 MySQL 和 Redis，可通过 Docker Compose 启动：

```bash
docker compose -f deploy/local/docker-compose.yml up -d
```

默认连接信息：

```text
MySQL: localhost:3306 / database vcoding / user vcoding / password vcoding
Redis: localhost:6379
```

### 结构验证

```bash
bash scripts/verify-structure.sh
```

### 前端

```bash
pnpm -C frontend install
pnpm -C frontend build
```

前端应用开发端口：

```text
auth-web: localhost:5173
portal-web: localhost:5174
demo-system-web: localhost:5175
```

### 后端

```bash
mvn -f backend/pom.xml validate
```

单独运行某个后端子模块前，建议先安装一次当前多模块产物，避免子模块读取到本地 Maven 仓库中的旧 `vcoding-common`：

```bash
mvn -f backend/pom.xml install -DskipTests
```

本地后端端口：

```text
vcoding-gateway: localhost:8080
vcoding-auth: localhost:8081
vcoding-system-demo: localhost:8082
```

本地调试时建议分别打开三个终端启动：

```bash
mvn -f backend/pom.xml -pl vcoding-auth spring-boot:run
mvn -f backend/pom.xml -pl vcoding-system-demo spring-boot:run
mvn -f backend/pom.xml -pl vcoding-gateway spring-boot:run
```

对外访问优先走 gateway：

```text
认证接口: http://localhost:8080/api/auth/**
示例系统: http://localhost:8080/api/demo/profile
```

认证接口 Swagger UI 直接访问 auth 服务：

```text
http://localhost:8081/swagger-ui/index.html
```

## 新增系统约定

新增一个业务系统时，建议同时补齐前后端模块：

- 前端应用：`frontend/apps/<system-name>-web`
- 后端模块：`backend/vcoding-<system-name>`

新增后端模块后，需要同步修改 `backend/pom.xml`，将模块加入 `<modules>`。新增前端应用后，需要确认 `frontend/pnpm-workspace.yaml` 的 `apps/*` 能覆盖该应用。

业务系统不应各自实现账号密码登录页，应统一跳转到 `frontend/apps/auth-web` 并复用统一用户中心能力。后端新增业务系统时，应接入 `vcoding-common` 中的内部 gateway 用户头校验，并通过 `AuthContext` 读取当前登录用户。

## 协作说明

根目录的 `AGENTS.md` 记录了本项目的默认语言、目录边界、前后端约定和统一登录边界。后续自动化协作或 AI 编码任务应优先遵循该文件。
