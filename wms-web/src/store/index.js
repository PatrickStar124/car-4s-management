import { createStore } from 'vuex'
import user from './user'

export default createStore({
    state: {
        // 可以在这里添加全局状态
    },
    mutations: {
        // 全局mutations
    },
    actions: {
        // 全局actions
    },
    modules: {
        user
    }
})