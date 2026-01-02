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
// ✅ 保持不变：这是正确的做法，确保了所有图标都能被全局使用。
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 初始化store - 确保在use之前调用
// 修复：从 localStorage 恢复用户状态
// ✅ 保持不变：这是标准的状态管理初始化流程。
store.dispatch('user/init')

// 使用插件
// ✅ 保持不变：插件的使用顺序是正确的。
app.use(store)
app.use(router)
app.use(ElementPlus)

// 全局配置
// ⚠️ 注意：Vue 3 中已不再推荐使用 app.config.globalProperties
// 但是对于 Element Plus 的全局配置，这种方式仍然有效。
// 为了保持与你现有代码的兼容性，我们暂时保留它。
app.config.globalProperties.$ELEMENT = {
    size: 'default'
}

// 最后挂载
// ✅ 保持不变：这是应用启动的最后一步。
app.mount('#app')