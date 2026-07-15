import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/trends'
  },
  {
    path: '/trends',
    name: 'global-trends',
    component: () => import('../views/TrendHomeView.vue')
  }
]
