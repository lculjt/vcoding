<script setup lang="ts">
import { computed } from 'vue'
import { DataAnalysis, List, Setting, Timer } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const currentUsername = computed(() => authStore.user?.username || '已登录用户')

const sourceCards = [
  { name: 'YouTube', status: '待接入', type: '视频热点' },
  { name: 'GitHub', status: '待接入', type: '项目趋势' }
]
</script>

<template>
  <div class="trend-app-shell">
    <aside class="trend-sidebar">
      <div class="sidebar-brand">
        <span class="brand-mark">GT</span>
        <div>
          <strong>热点观察台</strong>
          <span>GLOBAL TRENDS</span>
        </div>
      </div>
      <nav aria-label="系统导航">
        <a class="nav-item nav-item-active" href="#/trends">
          <el-icon><List /></el-icon>
          <span>今日热点</span>
        </a>
        <a class="nav-item" href="#/analytics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据洞察</span>
        </a>
        <a class="nav-item" href="#/collect-jobs">
          <el-icon><Timer /></el-icon>
          <span>采集任务</span>
        </a>
        <a class="nav-item" href="#/sources">
          <el-icon><Setting /></el-icon>
          <span>数据源配置</span>
        </a>
      </nav>
      <div class="sidebar-footer">
        <span class="status-dot" aria-hidden="true"></span>
        <span>系统骨架已就绪</span>
      </div>
    </aside>

    <main class="trend-shell">
      <header class="trend-header">
        <div>
          <p class="eyebrow">GLOBAL TREND MONITOR</p>
          <h1>海外热点聚合观察台</h1>
          <p class="header-description">统一查看海外平台热点，后续将补充趋势比对、中文摘要和用户归档。</p>
        </div>
        <div class="user-status">
          <span class="status-dot" aria-hidden="true"></span>
          <span>{{ currentUsername }}</span>
        </div>
      </header>

      <section class="workspace-grid" aria-label="系统准备状态">
        <article class="workspace-panel workspace-panel-primary">
          <div class="panel-heading">
            <div>
              <p class="panel-kicker">PHASE 1</p>
              <h2>一期采集工作台</h2>
            </div>
            <el-tag type="info" effect="plain">骨架已就绪</el-tag>
          </div>
          <p class="panel-copy">当前先完成统一登录、系统路由和数据源接入边界。采集任务、热点列表和图表模块将在后续阶段逐步开放。</p>
          <div class="metric-row">
            <div>
              <strong>2</strong>
              <span>首批数据源</span>
            </div>
            <div>
              <strong>0</strong>
              <span>已采集热点</span>
            </div>
            <div>
              <strong>每日</strong>
              <span>默认采集频率</span>
            </div>
          </div>
        </article>

        <article class="workspace-panel">
          <div class="panel-heading">
            <div>
              <p class="panel-kicker">DATA SOURCES</p>
              <h2>首批数据源</h2>
            </div>
            <el-tag type="warning" effect="plain">等待采集器</el-tag>
          </div>
          <ul class="source-list">
            <li v-for="source in sourceCards" :key="source.name">
              <div>
                <strong>{{ source.name }}</strong>
                <span>{{ source.type }}</span>
              </div>
              <el-tag size="small" effect="plain">{{ source.status }}</el-tag>
            </li>
          </ul>
        </article>
      </section>
    </main>
  </div>
</template>
