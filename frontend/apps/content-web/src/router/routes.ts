import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/topics'
  },
  {
    path: '/topics',
    name: 'topic-library',
    component: () => import('../views/TopicLibraryView.vue')
  },
  {
    path: '/topics/:id',
    name: 'topic-detail',
    component: () => import('../views/TopicDetailView.vue')
  }
]
