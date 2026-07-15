import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElCard } from 'element-plus'
import 'element-plus/dist/index.css'
import { VcodingOverlayPlugin } from '@vcoding/ui'
import '@vcoding/ui/styles'
import App from './App.vue'
import { router } from './router'
import './styles/main.scss'

createApp(App).use(createPinia()).use(router).use(ElCard).use(VcodingOverlayPlugin).mount('#app')
