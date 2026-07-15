import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { VcodingOverlayPlugin } from '@vcoding/ui'
import '@vcoding/ui/styles'
import App from './App.vue'
import { router } from './router'
import './styles/main.scss'

createApp(App).use(createPinia()).use(router).use(ElementPlus).use(VcodingOverlayPlugin).mount('#app')
