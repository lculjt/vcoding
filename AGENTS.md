# AGENTS.md

## 默认协作语言

默认使用中文回复。生成的文档、说明、注释尽量使用中文。代码标识符、第三方 API 名称、命令行参数、包名和模块名等必要内容保留英文。

## 项目定位

本仓库是 vcoding 平台项目，目标是承载多个业务系统。所有系统共用统一用户中心，并通过唯一登录界面完成账号密码登录。登录成功后，用户回到原目标系统；没有目标系统时进入默认业务系统。门户不是必经入口，可按产品需要启用。

## 技术栈与架构规范

涉及前端技术选型、后端技术选型、路由、状态管理、ORM、依赖同步、接口同步或新增系统架构时，必须先读取并遵守 `docs/architecture/technology-and-architecture.md`。

## 前端视觉规范

涉及登录页、门户、业务系统、后台管理页面、通用 UI 组件、样式主题或视觉改版时，必须先读取并遵守 `docs/design/visual-guidelines.md`。平台整体采用开发者工具风格：专业、克制、可信，带适度技术感，避免营销站式大渐变、大插画和过度装饰。

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
- 前端类型定义必须与逻辑代码分离；共享包类型放在 `src/types/`，请求封装放在 `src/request/`，业务接口函数放在 `src/api/`，包入口 `src/index.ts` 只做统一导出。
- 应用私有的页面表单类型和状态类型放在应用内 `src/types/`，跨应用复用后再沉淀到 workspace 共享包。
- AI 编写或修改 Vue 单文件组件时，单个 `.vue` 文件不得超过 800 行。
- 前端页面应按职责拆分组件，避免把页面布局、业务表单、状态切换和大段样式堆在同一个 `.vue` 文件中。
- 组件私有样式可以写在该组件的 `<style>` 标签中；跨应用复用的样式仍应优先沉淀到 `frontend/packages/ui`。

## 后端约定

- 后端构建工具使用 Maven。
- `backend/pom.xml` 是父模块，负责统一声明子模块和基础构建属性。
- 新增后端业务系统放在 `backend/vcoding-<system-name>`，并同步加入父 `pom.xml` 的 `<modules>`。
- 通用基础能力放在 `backend/vcoding-common`。
- 统一用户中心能力放在 `backend/vcoding-auth`。
- 统一入口、路由和鉴权边界放在 `backend/vcoding-gateway`。

## 编码注释约定

- Codex 编写或修改代码时，应补充必要的中文注释。
- 注释优先覆盖业务规则、权限边界、验证码/登录流程、关键配置、非直观实现原因以及后续需要替换或扩展的位置。
- 避免机械逐行注释，不为简单 getter/setter、直观赋值、常规框架注解等显而易见的代码添加冗余说明。
- 注释必须与当前代码行为一致；修改逻辑时应同步更新相关注释。

## 统一登录边界

- `frontend/apps/auth-web` 是唯一登录界面。
- `backend/vcoding-auth` 是统一用户中心后端模块。
- `frontend/apps/portal-web` 是可选门户入口；当前默认登录后进入业务系统。
- 业务系统未登录时应跳转到统一登录界面，而不是各自实现登录页。
- 业务系统跳转登录时应携带 `redirect`，登录成功后回到原业务系统；没有 `redirect` 时默认进入 `/demo` 或环境变量配置的默认系统。
- 后续新增系统时，应复用统一认证能力，不重复实现账号密码登录。

## Git 提交信息规则

Codex 生成 Git commit message 时，必须先读取并遵守 `docs/codex-commit-rules.md`。

## 常用验证命令

```bash
bash scripts/verify-structure.sh
mvn -f backend/pom.xml validate
pnpm -C frontend install
pnpm -C frontend build
```
