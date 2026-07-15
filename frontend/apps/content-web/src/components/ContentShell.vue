<script setup lang="ts">
import {
  Bell,
  Collection,
  Document,
  Files,
  HomeFilled,
  MagicStick,
  Search,
  Setting,
  Share
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { logoutAndRedirect } from '@vcoding/auth-client'
import { UserMenu } from '@vcoding/ui'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const authWebUrl = import.meta.env.VITE_AUTH_WEB_URL || 'http://localhost:5173/'

const navItems = [
  { label: '工作台', icon: HomeFilled, active: false },
  { label: '选题库', icon: Collection, active: true },
  { label: '内容生成', icon: MagicStick, active: false },
  { label: '素材资产', icon: Files, active: false },
  { label: '发布渠道', icon: Share, active: false },
  { label: '系统设置', icon: Setting, active: false }
]

const handleLogout = async () => {
  await logoutAndRedirect({
    authWebUrl,
    onLoggedOut: () => authStore.clearUser()
  })
}

const handleChangePassword = () => {
  // 修改密码后续会沉淀为统一弹窗能力，当前先保留统一入口位置。
  ElMessage.info('修改密码功能待接入统一用户中心接口')
}
</script>

<template>
  <div class="content-shell vc-page">
    <aside class="content-shell__sidebar">
      <div class="content-shell__brand">
        <div class="content-shell__brand-mark">
          <el-icon>
            <Document />
          </el-icon>
        </div>
        <div>
          <strong>AI 内容工作台</strong>
          <span>Content Ops</span>
        </div>
      </div>

      <nav class="content-shell__nav" aria-label="内容系统导航">
        <button
          v-for="item in navItems"
          :key="item.label"
          class="content-shell__nav-item"
          :class="{ 'content-shell__nav-item--active': item.active }"
          type="button"
        >
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>
    </aside>

    <section class="content-shell__main">
      <header class="content-shell__header">
        <div class="content-shell__search">
          <el-icon>
            <Search />
          </el-icon>
          <span>搜索选题、平台、关键词</span>
        </div>

        <div class="content-shell__actions">
          <button class="content-shell__icon-button" type="button" aria-label="通知">
            <el-icon>
              <Bell />
            </el-icon>
          </button>
          <UserMenu
            :username="authStore.user?.username"
            :subtitle="authStore.user?.adminFlag ? '平台管理员' : '内容创作者'"
            @logout="handleLogout"
            @change-password="handleChangePassword"
          />
        </div>
      </header>

      <main class="content-shell__content">
        <slot />
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.content-shell {
  display: grid;
  min-height: 100vh;
  grid-template-columns: 232px minmax(0, 1fr);
  background: #f5f7fb;
}

.content-shell__sidebar {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 22px 16px;
  border-right: 1px solid rgba(148, 163, 184, 0.16);
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.96), rgba(17, 24, 39, 0.98)),
    var(--vc-bg-page);
  color: var(--vc-text-inverse);
}

.content-shell__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 6px;
}

.content-shell__brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border: 1px solid rgba(20, 184, 166, 0.38);
  border-radius: 8px;
  background: rgba(20, 184, 166, 0.1);
  color: var(--vc-accent);
}

.content-shell__brand strong,
.content-shell__brand span {
  display: block;
}

.content-shell__brand strong {
  font-size: 15px;
  line-height: 22px;
}

.content-shell__brand span {
  color: var(--vc-text-inverse-muted);
  font-family: var(--vc-font-mono);
  font-size: 11px;
  line-height: 16px;
}

.content-shell__nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.content-shell__nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  height: 38px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  color: #cbd5e1;
  cursor: pointer;
  font-size: 14px;
  text-align: left;
}

.content-shell__nav-item:hover,
.content-shell__nav-item--active {
  border-color: rgba(37, 99, 235, 0.34);
  background: rgba(37, 99, 235, 0.16);
  color: #ffffff;
}

.content-shell__main {
  min-width: 0;
}

.content-shell__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 28px;
  border-bottom: 1px solid var(--vc-border);
  background: rgba(255, 255, 255, 0.92);
}

.content-shell__search {
  display: flex;
  align-items: center;
  width: min(420px, 48vw);
  height: 36px;
  gap: 8px;
  padding: 0 12px;
  border: 1px solid var(--vc-border);
  border-radius: 6px;
  background: var(--vc-bg-subtle);
  color: var(--vc-text-muted);
  font-size: 13px;
}

.content-shell__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.content-shell__icon-button {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid var(--vc-border);
  border-radius: 6px;
  background: #ffffff;
  color: var(--vc-text-muted);
  cursor: pointer;
}

.content-shell__icon-button:hover {
  border-color: var(--vc-border-strong);
  color: var(--vc-primary);
}

.content-shell__content {
  min-width: 0;
  padding: 28px;
}

@media (max-width: 900px) {
  .content-shell {
    grid-template-columns: 72px minmax(0, 1fr);
  }

  .content-shell__brand div:last-child,
  .content-shell__nav-item span,
  .content-shell__search span {
    display: none;
  }

  .content-shell__sidebar {
    align-items: center;
    padding-inline: 12px;
  }

  .content-shell__nav-item {
    justify-content: center;
    padding: 0;
  }

  .content-shell__header {
    padding: 0 16px;
  }

  .content-shell__search {
    width: 40px;
    justify-content: center;
    padding: 0;
  }

  .content-shell__content {
    padding: 18px;
  }
}
</style>
