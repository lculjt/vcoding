# AGENTS.md

## 默认协作语言

默认使用中文回复。生成的文档、说明、注释尽量使用中文。代码标识符、第三方 API 名称、命令行参数、包名和模块名等必要内容保留英文。

## 项目定位

本仓库是 vcoding 平台项目，目标是承载多个业务系统。所有系统共用统一用户中心，并通过唯一登录界面完成账号密码登录。登录成功后，用户再进入门户或具体业务系统。

## 目录边界

- `frontend/`：前端 monorepo，使用 pnpm workspace。
- `frontend/apps/`：可独立运行的前端应用，例如 `auth-web`、`portal-web` 和业务系统前端。
- `frontend/packages/`：跨应用共享包，例如 UI、认证客户端和通用工具。
- `backend/`：后端 Maven 父工程。
- `backend/vcoding-*`：后端 Maven 子模块。
- `docs/`：项目设计、计划和说明文档。
- `scripts/`：本地验证和辅助脚本。

## 前端约定

- 前端包管理器使用 pnpm。
- 新增前端应用放在 `frontend/apps/<system-name>-web`。
- 新增共享前端能力放在 `frontend/packages/<package-name>`。
- 登录态、Token、用户信息、跳转参数和权限判断应优先沉淀到 `frontend/packages/auth-client`。
- 通用 UI 组件应优先沉淀到 `frontend/packages/ui`。
- 通用类型、常量和工具函数应优先沉淀到 `frontend/packages/shared`。

## 后端约定

- 后端构建工具使用 Maven。
- `backend/pom.xml` 是父模块，负责统一声明子模块和基础构建属性。
- 新增后端业务系统放在 `backend/vcoding-<system-name>`，并同步加入父 `pom.xml` 的 `<modules>`。
- 通用基础能力放在 `backend/vcoding-common`。
- 统一用户中心能力放在 `backend/vcoding-auth`。
- 统一入口、路由和鉴权边界放在 `backend/vcoding-gateway`。

## 统一登录边界

- `frontend/apps/auth-web` 是唯一登录界面。
- `backend/vcoding-auth` 是统一用户中心后端模块。
- `frontend/apps/portal-web` 是登录后的系统入口。
- 业务系统未登录时应跳转到统一登录界面，而不是各自实现登录页。
- 后续新增系统时，应复用统一认证能力，不重复实现账号密码登录。

## Git 提交信息规则

Codex 生成 Git commit message 时，必须先读取并遵守 `docs/codex-commit-rules.md`。

## 常用验证命令

```bash
bash scripts/verify-structure.sh
mvn -f backend/pom.xml validate
pnpm -C frontend install --lockfile-only
```
