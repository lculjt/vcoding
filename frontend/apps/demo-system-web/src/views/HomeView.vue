<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const currentUsername = computed(() => authStore.user?.username || '已登录用户')
</script>

<template>
  <main class="demo-shell">
    <header class="demo-header">
      <div>
        <p class="eyebrow">demo-system-web</p>
        <h1>示例业务系统</h1>
      </div>
      <el-tag type="success">{{ currentUsername }}</el-tag>
    </header>

    <p>这个应用用于验证后续业务系统的接入方式。未登录直接访问时会跳转到统一登录页。</p>
  </main>
</template>
