import { defineStore } from 'pinia'
import type { CurrentUser } from '@vcoding/auth-client'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    authenticated: false,
    user: null as CurrentUser | null
  }),
  actions: {
    setAuthenticated(value: boolean) {
      this.authenticated = value
    },
    setSession(user: CurrentUser) {
      this.authenticated = true
      this.user = user
    },
    clearSession() {
      this.authenticated = false
      this.user = null
    }
  }
})
