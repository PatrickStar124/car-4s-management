import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/global.css'
import router from './router'
import store from './store'
import axios from "axios"
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'

// 优化：创建axios实例，避免全局配置污染，确保baseURL生效
const http = axios.create({
    baseURL: 'http://localhost:8090', // 后端地址
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    },
    withCredentials: true //  允许携带Cookie（后端需要的话）
})

const app = createApp(App)
const pinia = createPinia() // 创建Pinia实例

// 挂载axios实例到全局
app.config.globalProperties.$axios = http
app.config.globalProperties.$httpUrl = 'http://localhost:8090'

// 注册Element Plus和插件
app.use(ElementPlus, { size: 'small' })
app.use(router)
app.use(pinia) // 注册Pinia
if (store) { // 防止store不存在报错
    app.use(store)
}

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 挂载应用
app.mount('#app')