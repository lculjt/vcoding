# 平台化项目结构设计

## 背景

vcoding 项目计划承载多个业务系统。所有系统共用统一用户中心，并通过同一套用户账号密码登录。用户未登录时进入唯一登录界面，登录成功后再跳转到门户或具体业务系统。

当前仓库仍处于初始化阶段，已有文件较少，因此本次变更只建立长期可扩展的前后端骨架、根级协作说明和项目 README，不引入具体业务实现。

## 目标

- 仓库同时包含前端目录和后端目录。
- 前端使用 pnpm workspace 的 monorepo 模式。
- 后端使用 Maven 父工程和多模块模式。
- 明确统一用户中心、唯一登录界面和多业务系统扩展方式。
- 新增根级 `AGENTS.md`，并补全根级 `README.md`。

## 非目标

- 不实现真实登录、注册、权限、Token 或网关逻辑。
- 不引入数据库、缓存、消息队列等运行依赖。
- 不创建完整前端页面或后端业务接口。
- 不为某个具体业务系统设计领域模型。

## 目录设计

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
├── AGENTS.md
└── README.md
```

## 前端设计

前端采用 `frontend/` 下的 pnpm workspace。`apps/` 存放可独立运行的应用，`packages/` 存放跨应用共享能力。

- `frontend/apps/auth-web` 是唯一登录前端。所有需要登录的系统在未认证时都应跳转到这里。
- `frontend/apps/portal-web` 是登录后的系统入口，用于展示用户可访问的系统列表或工作台。
- `frontend/apps/demo-system-web` 是业务系统占位示例，用来说明后续新增系统的目录方式。
- `frontend/packages/ui` 存放通用 UI 组件。
- `frontend/packages/auth-client` 存放前端登录态、Token、用户信息、跳转参数和权限判断封装。
- `frontend/packages/shared` 存放通用类型、常量和工具函数。

后续新增前端系统时，在 `frontend/apps/<system-name>-web` 下新增应用，并优先复用 `ui`、`auth-client` 和 `shared`。

## 后端设计

后端采用 `backend/` 下的 Maven 父工程。根 `backend/pom.xml` 负责统一模块、版本和构建配置。

- `backend/vcoding-common` 存放通用基础能力，例如公共返回模型、异常、工具类和基础常量。
- `backend/vcoding-auth` 是统一用户中心模块，后续负责账号、密码登录、用户信息、角色权限、Token 和会话能力。
- `backend/vcoding-gateway` 是统一入口占位模块，后续可承担路由、统一鉴权、跨系统入口和认证转发等能力。
- `backend/vcoding-system-demo` 是业务系统后端占位示例，用来说明后续新增业务模块的方式。

后续新增后端系统时，在 `backend/vcoding-<system-name>` 下新增 Maven 子模块，并在父 `backend/pom.xml` 中声明。

## 认证与跳转边界

统一登录流程的长期方向如下：

1. 用户访问某个需要登录的业务系统。
2. 前端或网关发现用户未登录。
3. 用户被跳转到 `auth-web`。
4. `auth-web` 调用 `vcoding-auth` 完成账号密码登录。
5. 登录成功后，用户回到原业务系统或进入 `portal-web`。
6. 业务系统通过统一登录态识别当前用户。

本次只在目录、文档和模块命名中固化这个边界，不实现流程细节。

## 测试与验证

本次是项目骨架变更，验证重点是结构完整性和基础配置可读性：

- 检查前端 workspace 文件能覆盖 `apps/*` 和 `packages/*`。
- 检查后端父 `pom.xml` 声明所有子模块。
- 检查每个 Maven 子模块都有自己的 `pom.xml`。
- 检查 README 能说明项目结构、模块职责和扩展方式。
- 检查 `AGENTS.md` 能说明默认协作语言、目录约定和开发注意事项。

## 风险与取舍

当前设计预留了 `vcoding-gateway`，但不立即引入 Spring Cloud 或复杂网关依赖，避免早期过度设计。这样可以先保留统一入口边界，等认证、系统数量和部署方式更明确后再选择具体技术。

前端预留 `auth-client` 包，避免每个系统重复处理登录态和跳转规则。初期它可以只是空包或说明性骨架，后续随着认证流程落地再扩展实现。
