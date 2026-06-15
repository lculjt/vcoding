# Platform Structure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立 vcoding 的前端 pnpm workspace、后端 Maven 多模块父工程、根级协作说明和完整 README。

**Architecture:** 仓库根目录只保留总控文档和跨端说明，前端所有应用与共享包都放在 `frontend/`，后端所有 Maven 模块都放在 `backend/`。统一用户中心由 `auth-web` 和 `vcoding-auth` 表达，后续业务系统按同样目录模式扩展。

**Tech Stack:** pnpm workspace、Maven multi-module、Markdown、Shell 验证脚本。

---

### Task 1: 结构验证脚本

**Files:**
- Create: `scripts/verify-structure.sh`

- [ ] **Step 1: 写入失败优先的结构检查脚本**

创建 `scripts/verify-structure.sh`，检查本次骨架要求的目录和配置文件是否存在，并检查关键配置内容。

- [ ] **Step 2: 运行脚本确认失败**

Run: `bash scripts/verify-structure.sh`
Expected: FAIL，因为 `frontend/`、`backend/`、`AGENTS.md` 等目标文件还不存在。

### Task 2: 前端 pnpm workspace 骨架

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/pnpm-workspace.yaml`
- Create: `frontend/apps/auth-web/package.json`
- Create: `frontend/apps/portal-web/package.json`
- Create: `frontend/apps/demo-system-web/package.json`
- Create: `frontend/packages/ui/package.json`
- Create: `frontend/packages/auth-client/package.json`
- Create: `frontend/packages/shared/package.json`

- [ ] **Step 1: 创建前端目录和 workspace 配置**

创建 `frontend/`，配置 workspace 覆盖 `apps/*` 和 `packages/*`。

- [ ] **Step 2: 创建前端应用和共享包 package 文件**

为唯一登录页、门户、示例业务系统、UI 包、认证客户端包和共享包创建最小 `package.json`。

### Task 3: 后端 Maven 多模块骨架

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/vcoding-common/pom.xml`
- Create: `backend/vcoding-auth/pom.xml`
- Create: `backend/vcoding-gateway/pom.xml`
- Create: `backend/vcoding-system-demo/pom.xml`

- [ ] **Step 1: 创建 Maven 父工程**

父工程使用 `pom` packaging，统一声明四个子模块。

- [ ] **Step 2: 创建 Maven 子模块**

每个子模块继承父工程，使用最小 jar 模块配置。

### Task 4: 根级文档

**Files:**
- Create: `AGENTS.md`
- Modify: `README.md`
- Create: `.gitignore`

- [ ] **Step 1: 创建根级 AGENTS.md**

写明默认中文协作、目录边界、前端 workspace 规则、后端 Maven 模块规则和新增系统约定。

- [ ] **Step 2: 补全 README.md**

说明项目定位、目录结构、模块职责、开发命令、扩展方式和当前状态。

- [ ] **Step 3: 创建 .gitignore**

忽略前端依赖、构建产物、后端 target、IDE 和系统临时文件。

### Task 5: 验证

**Files:**
- Test: `scripts/verify-structure.sh`

- [ ] **Step 1: 运行结构验证**

Run: `bash scripts/verify-structure.sh`
Expected: PASS。

- [ ] **Step 2: 运行 Maven 验证**

Run: `mvn -f backend/pom.xml validate`
Expected: BUILD SUCCESS。

- [ ] **Step 3: 运行 pnpm workspace 验证**

Run: `pnpm -C frontend install --lockfile-only`
Expected: 生成或更新 `frontend/pnpm-lock.yaml`，workspace 可被 pnpm 识别。
