import { defineStore } from 'pinia'
import type { CurrentUser } from '@vcoding/auth-client'

export const useAuthStore = defineStore('contentAuth', {
  state: () => ({
    user: null as CurrentUser | null
  }),
  actions: {
    setUser(user: CurrentUser) {
      this.user = user
    },
    clearUser() {
      this.user = null
    }
  }
})
