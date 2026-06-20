# Setup Captcha Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `vcoding-auth` 中建立图形验证码、短信验证码、Redis 存储、冷却限流和短信发送器抽象。

**Architecture:** `api` 暴露验证码 HTTP 接口，`application` 编排验证码生成、存储、校验和发送，`domain` 定义短信场景与短信发送接口，`infrastructure` 提供本地 `ConsoleSmsSender`。验证码数据存入 Redis，接口统一返回 `ApiResponse`。

**Tech Stack:** Java 17、Spring Boot 3、Spring Web MVC、Spring Data Redis、Lombok、Maven。

---

### Task 1: 错误码和配置

**Files:**
- Modify: `backend/vcoding-common/src/main/java/com/vcoding/common/response/ErrorCode.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/config/CaptchaProperties.java`
- Modify: `backend/vcoding-auth/src/main/resources/application.yml`

- [ ] **Step 1: 补充验证码错误码**

新增图形验证码、短信验证码、冷却和发送次数相关错误码。

- [ ] **Step 2: 创建 CaptchaProperties**

集中管理图形验证码 TTL、短信验证码 TTL、冷却时间、发送限制和校验失败限制。

### Task 2: 图形验证码

**Files:**
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/application/captcha/CaptchaHashService.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/application/captcha/ImageCaptchaService.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/api/dto/ImageCaptchaResponse.java`

- [ ] **Step 1: 实现验证码 hash 工具**

使用 SHA-256 保存验证码摘要，不在 Redis 中保存明文。

- [ ] **Step 2: 实现图形验证码生成**

生成验证码 ID、4 位验证码和 SVG data URL，hash 后写入 Redis。

### Task 3: 短信验证码

**Files:**
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/domain/sms/SmsScene.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/domain/sms/SmsSender.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/infrastructure/sms/ConsoleSmsSender.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/application/captcha/SmsCodeService.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/api/dto/SendSmsCodeRequest.java`
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/api/dto/SendSmsCodeResponse.java`

- [ ] **Step 1: 定义短信场景和发送器接口**

支持 register、login、reset-password、bind-phone、change-phone。

- [ ] **Step 2: 实现 ConsoleSmsSender**

本地输出脱敏手机号和验证码场景。

- [ ] **Step 3: 实现 SmsCodeService**

校验图形验证码，执行冷却和限流，生成 6 位验证码，hash 后存 Redis。

### Task 4: API 接口与验证

**Files:**
- Create: `backend/vcoding-auth/src/main/java/com/vcoding/auth/api/CaptchaController.java`
- Modify: `scripts/verify-structure.sh`

- [ ] **Step 1: 新增验证码接口**

提供 `GET /api/auth/captcha/image` 和 `POST /api/auth/sms/send`。

- [ ] **Step 2: 更新结构验证脚本并运行验证**

Run:

```bash
bash scripts/verify-structure.sh
mvn -f backend/pom.xml validate
mvn -f backend/pom.xml compile
```

Expected: 全部成功。
