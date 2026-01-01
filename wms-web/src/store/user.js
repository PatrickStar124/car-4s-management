// src/store/user.js - 用户状态管理
export default {
    namespaced: true, // 启用命名空间

    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('user') || '{}'),
        isAuthenticated: false
    }),

    getters: {
        // 是否已认证
        isAuthenticated: state => !!state.token,
        // 用户名
        userName: state => state.userInfo.name || state.userInfo.no || '',
        // 用户角色
        userRole: state => state.userInfo.role || '',
        // 用户ID
        userId: state => state.userInfo.id || ''
    },

    mutations: {
        // 设置用户信息
        SET_USER(state, userData) {
            state.userInfo = userData
            state.token = userData.token || `token-${userData.id || Date.now()}`
            state.isAuthenticated = true

            // 保存到localStorage
            localStorage.setItem('user', JSON.stringify(userData))
            localStorage.setItem('token', state.token)
        },

        // 清除用户信息
        CLEAR_USER(state) {
            state.userInfo = {}
            state.token = ''
            state.isAuthenticated = false

            // 从localStorage清除
            localStorage.removeItem('user')
            localStorage.removeItem('token')
        },

        // 更新用户信息
        UPDATE_USER_INFO(state, userData) {
            state.userInfo = { ...state.userInfo, ...userData }
            localStorage.setItem('user', JSON.stringify(state.userInfo))
        }
    },

    actions: {
        // 登录成功后设置用户
        setUser({ commit }, userData) {
            commit('SET_USER', userData)
        },

        // 退出登录
        logout({ commit }) {
            commit('CLEAR_USER')
        },

        // 更新用户信息
        updateUserInfo({ commit }, userData) {
            commit('UPDATE_USER_INFO', userData)
        },

        // 初始化用户状态（从localStorage恢复）
        initUser({ commit }) {
            const token = localStorage.getItem('token')
            const user = localStorage.getItem('user')

            if (token && user) {
                try {
                    const userData = JSON.parse(user)
                    commit('SET_USER', userData)
                } catch (error) {
                    console.error('恢复用户状态失败:', error)
                    commit('CLEAR_USER')
                }
            }
        }
    }
}