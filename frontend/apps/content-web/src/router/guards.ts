import type { Router } from 'vue-router'
import { getCurrentUser, redirectToLogin } from '@vcoding/auth-client'
import { useAuthStore } from '../stores/auth'

const authWebUrl = import.meta.env.VITE_AUTH_WEB_URL || 'http://localhost:5173/'

export function setupRouterGuards(router: Router): void {
  router.beforeEach(async () => {
    const authStore = useAuthStore()

    if (authStore.user) {
      return true
    }

    try {
      const currentUser = await getCurrentUser()
      authStore.setUser(currentUser)
      return true
    } catch {
      // 内容系统只消费统一登录态，未登录时回到 auth-web，并保留当前访问地址。
      redirectToLogin({ authWebUrl })
      return false
    }
  })
}
