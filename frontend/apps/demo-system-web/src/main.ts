import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElTag } from 'element-plus'
import 'element-plus/dist/index.css'
import '@vcoding/ui/styles'
import App from './App.vue'
import { router } from './router'
import './styles/main.scss'

createApp(App).use(createPinia()).use(router).use(ElTag).mount('#app')
