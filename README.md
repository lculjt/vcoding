# vcoding

vcoding 是一个面向多业务系统的平台型项目。项目计划支持多个系统共存，但所有系统共用统一用户中心，并通过唯一登录界面完成账号密码登录。用户登录成功后，再进入门户或具体业务系统。

当前仓库处于初始化骨架阶段，重点是建立清晰的前后端目录边界和后续扩展约定。

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

## 统一登录流程

长期目标中的登录边界如下：

1. 用户访问某个需要登录的业务系统。
2. 前端应用或网关发现用户未登录。
3. 用户跳转到 `auth-web`。
4. `auth-web` 调用 `vcoding-auth` 完成账号密码登录。
5. 登录成功后，用户回到原业务系统或进入 `portal-web`。
6. 业务系统通过统一登录态识别当前用户。

当前版本只建立目录和模块骨架，不包含真实登录、权限、Token、数据库或网关实现。

## 开发命令

### 结构验证

```bash
bash scripts/verify-structure.sh
```

### 前端

```bash
pnpm -C frontend install --lockfile-only
pnpm -C frontend build
```

### 后端

```bash
mvn -f backend/pom.xml validate
```

## 新增系统约定

新增一个业务系统时，建议同时补齐前后端模块：

- 前端应用：`frontend/apps/<system-name>-web`
- 后端模块：`backend/vcoding-<system-name>`

新增后端模块后，需要同步修改 `backend/pom.xml`，将模块加入 `<modules>`。新增前端应用后，需要确认 `frontend/pnpm-workspace.yaml` 的 `apps/*` 能覆盖该应用。

业务系统不应各自实现账号密码登录页，应统一跳转到 `frontend/apps/auth-web` 并复用统一用户中心能力。

## 协作说明

根目录的 `AGENTS.md` 记录了本项目的默认语言、目录边界、前后端约定和统一登录边界。后续自动化协作或 AI 编码任务应优先遵循该文件。
