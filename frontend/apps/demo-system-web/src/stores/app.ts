import { defineStore } from 'pinia'

export const useAppStore = defineStore('demoSystemApp', {
  state: () => ({
    ready: true
  })
})
