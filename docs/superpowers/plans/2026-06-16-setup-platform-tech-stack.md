# Setup Platform Tech Stack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将当前前后端骨架升级为第一阶段可开发的 Vue 3/Vite/TypeScript 与 Spring Boot 3/Maven 多模块基础，并补齐本地 MySQL/Redis 环境。

**Architecture:** 前端仍保持 `frontend/apps/*` 与 `frontend/packages/*` 的 pnpm workspace；三个应用都升级为最小 Vue 3 + Vite 应用，共享包提供 TypeScript 导出。后端父工程统一管理 Spring Boot、MyBatis-Plus、Flyway、Redis、OpenAPI 等依赖版本，`vcoding-auth` 作为第一个可启动 Spring Boot 应用，其他模块保持可构建。

**Tech Stack:** Vue 3、TypeScript、Vite、Vue Router、Pinia、Element Plus、Axios、SCSS、pnpm workspace、Java 17、Spring Boot 3、Maven、MyBatis-Plus、Flyway、Redis、MySQL、Docker Compose。

---

### Task 1: 验证脚本

**Files:**
- Create: `scripts/verify-structure.sh`

- [ ] **Step 1: 创建结构验证脚本**

检查前端 workspace、Vue 应用入口、共享包导出、后端 Maven 模块、Spring Boot 启动类和本地 Docker Compose 是否存在。

- [ ] **Step 2: 运行脚本确认当前缺失项**

Run: `bash scripts/verify-structure.sh`
Expected: FAIL，因为 Vue 应用源码、Spring Boot 启动类和 Docker Compose 尚未创建。

### Task 2: 前端 Vue 3 workspace

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/apps/auth-web/package.json`
- Modify: `frontend/apps/portal-web/package.json`
- Modify: `frontend/apps/demo-system-web/package.json`
- Create app source files under each `frontend/apps/*`
- Modify package files under `frontend/packages/*`
- Create shared package source files

- [ ] **Step 1: 配置 workspace 统一脚本和依赖**

根 `frontend/package.json` 保持聚合 build/dev/lint，应用和包声明 Vue 3、Vite、TypeScript、Vue Router、Pinia、Element Plus、Axios、SCSS 等依赖。

- [ ] **Step 2: 创建三个最小 Vue 应用**

每个应用提供 `index.html`、`src/main.ts`、`src/App.vue`、`src/router`、`src/stores`、`src/styles`、`vite.config.ts`、`tsconfig.json`。

- [ ] **Step 3: 创建共享包基础导出**

`ui`、`auth-client`、`shared` 提供 `src/index.ts` 和 TypeScript 构建配置。

### Task 3: 后端 Spring Boot 3 多模块基础

**Files:**
- Modify: `backend/pom.xml`
- Modify child module POM files
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/VcodingAuthApplication.java`
- Create config files under `backend/vcoding-auth/src/main/resources`

- [ ] **Step 1: 父 POM 增加 Spring Boot 依赖管理和插件管理**

统一管理 Spring Boot、MyBatis-Plus、Flyway、springdoc-openapi、JWT 等依赖版本。

- [ ] **Step 2: 子模块声明需要的依赖**

`vcoding-auth` 接入 Web、Security、Validation、MyBatis-Plus、Flyway、MySQL、Redis、OpenAPI；公共模块保持基础依赖。

- [ ] **Step 3: 创建 `vcoding-auth` 启动类和 local/dev/prod 配置**

启动类只负责可启动，暂不实现认证业务。

### Task 4: 本地环境与文档

**Files:**
- Create: `deploy/local/docker-compose.yml`
- Modify: `README.md`

- [ ] **Step 1: 添加 MySQL + Redis 本地 Docker Compose**

MySQL 使用 `vcoding` 数据库和用户，Redis 暴露本地端口。

- [ ] **Step 2: 更新 README 本地启动说明**

补充 Docker Compose、后端 validate、前端 install/build/dev 命令。

### Task 5: 验证

**Files:**
- Test: `scripts/verify-structure.sh`

- [ ] **Step 1: 运行结构验证**

Run: `bash scripts/verify-structure.sh`
Expected: PASS。

- [ ] **Step 2: 运行后端验证**

Run: `mvn -f backend/pom.xml validate`
Expected: BUILD SUCCESS。

- [ ] **Step 3: 运行前端安装和构建**

Run: `pnpm -C frontend install && pnpm -C frontend build`
Expected: install 和 build 成功。
