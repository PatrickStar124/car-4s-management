import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 先创建app实例
const app = createApp(App)

// 然后使用插件
app.use(store)
app.use(router)
app.use(ElementPlus)

// 最后挂载
app.mount('#app')