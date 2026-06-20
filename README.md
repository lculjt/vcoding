# vcoding

vcoding 是一个面向多业务系统的平台型项目。项目计划支持多个系统共存，但所有系统共用统一用户中心，并通过唯一登录界面完成账号密码登录或手机号验证码登录。用户登录成功后，再进入门户或具体业务系统。

当前仓库已建立前后端工程骨架，并在 `vcoding-auth` 中落地统一用户中心的第一批接口能力，包括图形验证码、短信验证码、自助注册、登录、当前用户和退出登录。

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
- `frontend/apps/portal-web`：登录后的系统入口或工作台。
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
5. 登录成功后，用户回到原业务系统或进入 `portal-web`。
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
```

登录成功后，`vcoding-auth` 通过 `Set-Cookie` 写入 `VCODING_TOKEN`。该 Cookie 使用 HttpOnly 模式，前端不读取 JWT 明文，后续请求通过浏览器自动携带登录态。

接口文档由 springdoc-openapi 自动生成，本地启动 `vcoding-auth` 后可访问：

```text
Swagger UI: http://localhost:8080/swagger-ui/index.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

业务系统接口的统一鉴权边界后续放在 `vcoding-gateway` 或公共鉴权过滤器中。业务系统不应重复实现登录，只应基于当前登录用户做业务授权判断。

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

统一用户中心后端启动模块：

```bash
mvn -f backend/pom.xml -pl vcoding-auth spring-boot:run
```

启动后可通过 Swagger UI 查看和调试认证接口：

```text
http://localhost:8080/swagger-ui/index.html
```

## 新增系统约定

新增一个业务系统时，建议同时补齐前后端模块：

- 前端应用：`frontend/apps/<system-name>-web`
- 后端模块：`backend/vcoding-<system-name>`

新增后端模块后，需要同步修改 `backend/pom.xml`，将模块加入 `<modules>`。新增前端应用后，需要确认 `frontend/pnpm-workspace.yaml` 的 `apps/*` 能覆盖该应用。

业务系统不应各自实现账号密码登录页，应统一跳转到 `frontend/apps/auth-web` 并复用统一用户中心能力。

## 协作说明

根目录的 `AGENTS.md` 记录了本项目的默认语言、目录边界、前后端约定和统一登录边界。后续自动化协作或 AI 编码任务应优先遵循该文件。
