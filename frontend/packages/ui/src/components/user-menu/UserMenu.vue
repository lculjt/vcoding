<script setup lang="ts">
import { computed } from 'vue'
import { ArrowDown, Lock, SwitchButton, UserFilled } from '@element-plus/icons-vue'

import type { UserMenuCommand } from '../../types/user-menu'

const props = withDefaults(
  defineProps<{
    username?: string
    displayName?: string
    avatarUrl?: string
    subtitle?: string
    compact?: boolean
  }>(),
  {
    username: '',
    displayName: '',
    avatarUrl: '',
    subtitle: '',
    compact: false
  }
)

const emit = defineEmits<{
  command: [command: UserMenuCommand]
  changePassword: []
  logout: []
}>()

const visibleName = computed(() => props.displayName || props.username || '未命名用户')
const avatarText = computed(() => visibleName.value.slice(0, 1).toUpperCase())

/**
 * 用户菜单只负责把用户操作转换为事件，真正的改密弹窗、退出接口和跳转由具体应用处理。
 */
const handleCommand = (command: UserMenuCommand) => {
  emit('command', command)
  if (command === 'change-password') {
    emit('changePassword')
  }
  if (command === 'logout') {
    emit('logout')
  }
}
</script>

<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <button class="vc-user-menu__trigger" type="button">
      <el-avatar v-if="avatarUrl" class="vc-user-menu__avatar" :size="compact ? 28 : 32" :src="avatarUrl" />
      <el-avatar v-else class="vc-user-menu__avatar" :size="compact ? 28 : 32">
        {{ avatarText }}
        <el-icon v-if="!avatarText">
          <UserFilled />
        </el-icon>
      </el-avatar>

      <span class="vc-user-menu__text">
        <span class="vc-user-menu__name">{{ visibleName }}</span>
        <span v-if="subtitle && !compact" class="vc-user-menu__subtitle">{{ subtitle }}</span>
      </span>

      <el-icon class="vc-user-menu__arrow">
        <ArrowDown />
      </el-icon>
    </button>

    <template #dropdown>
      <el-dropdown-menu class="vc-user-menu__dropdown">
        <el-dropdown-item command="change-password">
          <el-icon class="vc-user-menu__item-icon">
            <Lock />
          </el-icon>
          <span>修改密码</span>
        </el-dropdown-item>
        <el-dropdown-item command="logout" divided>
          <el-icon class="vc-user-menu__item-icon vc-user-menu__item-icon--danger">
            <SwitchButton />
          </el-icon>
          <span>退出登录</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>
