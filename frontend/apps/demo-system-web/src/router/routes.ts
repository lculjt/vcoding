import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'demo-system',
    component: () => import('../views/HomeView.vue')
  }
]
