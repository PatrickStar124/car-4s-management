import { createStore } from 'vuex'
import user from './user'

export default createStore({
    modules: {
        user
    },

    // 根级状态
    state: {
        loading: false,
        error: null
    },

    // 根级getters
    getters: {
        isLoading: state => state.loading,
        errorMessage: state => state.error
    },

    // 根级mutations
    mutations: {
        SET_LOADING(state, loading) {
            state.loading = loading
        },
        SET_ERROR(state, error) {
            state.error = error
        },
        CLEAR_ERROR(state) {
            state.error = null
        }
    },

    // 根级actions
    actions: {
        // 全局loading状态
        setLoading({ commit }, loading) {
            commit('SET_LOADING', loading)
        },

        // 全局错误处理
        setError({ commit }, error) {
            commit('SET_ERROR', error)
            // 5秒后自动清除错误
            setTimeout(() => {
                commit('CLEAR_ERROR')
            }, 5000)
        },

        clearError({ commit }) {
            commit('CLEAR_ERROR')
        },

        // 初始化应用
        async initApp({ dispatch }) {
            try {
                // 可以在这里初始化其他模块
                await Promise.all([
                    dispatch('user/init')
                ])
                console.log('✅ 应用初始化完成')
            } catch (error) {
                console.error('应用初始化失败:', error)
            }
        }
    }
})