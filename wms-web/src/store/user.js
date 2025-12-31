// src/store/user.js - 修复版

// 模拟用户数据
const mockUsers = {
    // 车主
    'user001': { id: 1, no: 'user001', name: '张三车主', password: '123456', role: 'owner' },
    // 员工
    'staff001': { id: 2, no: 'staff001', name: '李四顾问', password: '123456', role: 'service' }
}

export default {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
    }),

    getters: {
        isAuthenticated: state => !!state.token,
        userName: state => state.userInfo.name || '',
        userRole: state => state.userInfo.role || ''
    },

    mutations: {
        SET_USER(state, userData) {
            state.token = userData.token || `user-${userData.id || Date.now()}`
            state.userInfo = userData
            localStorage.setItem('token', state.token)
            localStorage.setItem('userInfo', JSON.stringify(userData))
        },

        CLEAR_USER(state) {
            state.token = ''
            state.userInfo = {}
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            localStorage.removeItem('userType')
        }
    },

    actions: {
        // 模拟登录 - 移除不必要的 try/catch
        async login({ commit }, loginForm) {
            // 模拟网络延迟
            await new Promise(resolve => setTimeout(resolve, 500))

            const username = loginForm.username
            const password = loginForm.password
            const user = mockUsers[username]

            if (user && user.password === password) {
                const userData = {
                    ...user,
                    token: `token-${username}-${Date.now()}`
                }

                commit('SET_USER', userData)

                return {
                    success: true,
                    data: userData,
                    message: '登录成功'
                }
            } else {
                throw new Error('用户名或密码错误')
            }
        },

        // 模拟注册 - 移除不必要的 try/catch
        async userRegister({ commit }, registerForm) {
            // 模拟网络延迟
            await new Promise(resolve => setTimeout(resolve, 500))

            // 检查用户名是否已存在
            if (mockUsers[registerForm.username]) {
                throw new Error('用户编号已存在')
            }

            // 创建新用户
            const newUserId = Object.keys(mockUsers).length + 1
            const userData = {
                id: newUserId,
                no: registerForm.username,
                name: registerForm.name,
                password: registerForm.password,
                phone: registerForm.phone || '',
                role: 'owner',
                token: `token-${registerForm.username}-${Date.now()}`
            }

            // 添加到模拟数据库
            mockUsers[registerForm.username] = userData

            // 自动登录
            commit('SET_USER', userData)

            return {
                success: true,
                data: userData,
                message: '注册成功'
            }
        },

        // 退出登录 - 简化版本
        async userLogout({ commit }) {
            // 模拟网络延迟
            await new Promise(resolve => setTimeout(resolve, 300))

            commit('CLEAR_USER')
            // 重定向到登录页
            window.location.href = '/login'
        }
    }
}