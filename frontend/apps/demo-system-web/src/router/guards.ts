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
      // 业务系统不实现登录页，未登录统一跳到 auth-web，并携带原始访问地址。
      redirectToLogin({ authWebUrl })
      return false
    }
  })
}
