import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 创建app实例
const app = createApp(App)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 使用插件
app.use(store)
app.use(router)
app.use(ElementPlus)

// 全局配置
app.config.globalProperties.$ELEMENT = {
    size: 'default'
}

// 最后挂载
app.mount('#app')

// 页面加载时恢复登录状态
store.dispatch('restoreLogin')