import authApi from '@/api/auth'
import { ElMessage } from 'element-plus'

export default {
    namespaced: true,

    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('user') || '{}'),
        isAuthenticated: !!localStorage.getItem('token')
    }),

    getters: {
        isAuthenticated: state => state.isAuthenticated,
        userName: state => state.userInfo.name || state.userInfo.no || '',
        userRole: state => state.userInfo.role || '',
        userId: state => state.userInfo.id || '',
        userNo: state => state.userInfo.no || '',

        // 角色文本
        roleText: state => {
            const roles = {
                owner: '车主',
                service: '服务顾问',
                mechanic: '维修技师',
                warehouse: '仓库管理员',
                admin: '管理员'
            }
            return roles[state.userInfo.role] || '用户'
        },

        // 判断是否是员工（非车主）
        isStaff: state => {
            const role = state.userInfo.role
            return role && role !== 'owner'
        },

        // 判断是否是车主
        isOwner: state => state.userInfo.role === 'owner',

        // 判断是否是服务顾问
        isServiceAdvisor: state => state.userInfo.role === 'service',

        // 判断是否是维修技师
        isMechanic: state => state.userInfo.role === 'mechanic',

        // 判断是否是仓库管理员
        isWarehouse: state => state.userInfo.role === 'warehouse',

        // 判断是否是管理员
        isAdmin: state => state.userInfo.role === 'admin'
    },

    mutations: {
        SET_USER(state, userData) {
            state.userInfo = { ...state.userInfo, ...userData }
            state.token = userData.token || state.token || ''
            state.isAuthenticated = true

            localStorage.setItem('user', JSON.stringify(state.userInfo))
            if (state.token) {
                localStorage.setItem('token', state.token)
            }

            console.log('✅ 用户状态已设置:', state.userInfo)
        },

        CLEAR_USER(state) {
            state.userInfo = {}
            state.token = ''
            state.isAuthenticated = false

            localStorage.removeItem('user')
            localStorage.removeItem('token')

            console.log('✅ 用户状态已清除')
        },

        UPDATE_USER(state, userData) {
            state.userInfo = { ...state.userInfo, ...userData }
            localStorage.setItem('user', JSON.stringify(state.userInfo))
        },

        SET_TOKEN(state, token) {
            state.token = token
            localStorage.setItem('token', token)
        }
    },

    actions: {
        // 登录 - 修复：移除了未使用的 dispatch 参数
        async login({ commit }, credentials) {
            try {
                console.log('🔐 正在登录:', credentials)
                const response = await authApi.login(credentials)

                if (response.code === 200) {
                    const userData = response.data || response

                    // 如果没有token，创建一个模拟token
                    if (!userData.token) {
                        userData.token = `jwt-mock-${userData.id || Date.now()}`
                    }

                    commit('SET_USER', userData)

                    // 显示欢迎消息
                    const userName = userData.name || userData.no
                    ElMessage.success(`欢迎回来，${userName}！`)

                    return {
                        success: true,
                        data: userData,
                        message: '登录成功'
                    }
                } else {
                    const message = response.message || response.msg || '登录失败'
                    ElMessage.error(message)
                    return {
                        success: false,
                        message: message
                    }
                }
            } catch (error) {
                console.error('登录请求失败:', error)
                const message = error.message || '网络请求失败，请检查服务器'
                ElMessage.error(message)
                return {
                    success: false,
                    message: message
                }
            }
        },

        // 注册
        async register({ commit }, userData) {
            try {
                console.log('📝 正在注册:', userData)
                const response = await authApi.register(userData)

                if (response.code === 200) {
                    const userData = response.data || response

                    // 如果没有token，创建一个模拟token
                    if (!userData.token) {
                        userData.token = `jwt-mock-${userData.id || Date.now()}`
                    }

                    commit('SET_USER', userData)

                    ElMessage.success('注册成功！')

                    return {
                        success: true,
                        data: userData,
                        message: '注册成功'
                    }
                } else {
                    const message = response.message || response.msg || '注册失败'
                    ElMessage.error(message)
                    return {
                        success: false,
                        message: message
                    }
                }
            } catch (error) {
                console.error('注册请求失败:', error)
                const message = error.message || '网络请求失败，请检查服务器'
                ElMessage.error(message)
                return {
                    success: false,
                    message: message
                }
            }
        },

        // 获取用户信息
        async fetchUserInfo({ commit, state }) {
            if (!state.userId) {
                console.warn('⚠️ 没有用户ID，跳过获取用户信息')
                return
            }

            try {
                const response = await authApi.getUserInfo(state.userId)
                if (response.code === 200) {
                    commit('UPDATE_USER', response.data || response)
                    console.log('✅ 用户信息已更新')
                }
            } catch (error) {
                console.error('获取用户信息失败:', error)
            }
        },

        // 更新用户信息
        async updateUserInfo({ commit }, userData) {
            try {
                const response = await authApi.updateUser(userData)
                if (response.code === 200) {
                    commit('UPDATE_USER', userData)
                    ElMessage.success('信息更新成功')
                    return { success: true }
                } else {
                    const message = response.message || response.msg || '更新失败'
                    ElMessage.error(message)
                    return { success: false, message }
                }
            } catch (error) {
                console.error('更新用户信息失败:', error)
                const message = error.message || '网络请求失败'
                ElMessage.error(message)
                return { success: false, message }
            }
        },

        // 登出
        logout({ commit }) {
            commit('CLEAR_USER')
            ElMessage.success('已安全退出')
            return Promise.resolve()
        },

        // 初始化 - 修复：移除了未使用的 state 参数
        init({ commit }) {
            try {
                const token = localStorage.getItem('token')
                const userStr = localStorage.getItem('user')

                if (token && userStr) {
                    const userData = JSON.parse(userStr)

                    // 验证数据完整性
                    if (userData.id && userData.role) {
                        commit('SET_USER', {
                            ...userData,
                            token: token
                        })
                        console.log('✅ 用户状态已从本地存储恢复')
                        return true
                    } else {
                        console.warn('⚠️ 本地存储的用户数据不完整，清除状态')
                        commit('CLEAR_USER')
                        return false
                    }
                } else {
                    console.log('ℹ️ 没有找到本地存储的用户数据')
                    return false
                }
            } catch (error) {
                console.error('❌ 恢复用户状态失败:', error)
                localStorage.removeItem('user')
                localStorage.removeItem('token')
                commit('CLEAR_USER')
                return false
            }
        }
    }
}