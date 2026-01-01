import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'
import { ElMessage } from 'element-plus'

const routes = [
    {
        path: '/',
        redirect: '/home'
    },
    {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/HomePage.vue'),
        meta: {
            title: '首页'
        }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/LoginPage.vue'),
        meta: {
            title: '登录',
            guestOnly: true
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/RegisterPage.vue'),
        meta: {
            title: '车主注册',
            guestOnly: true
        }
    },
    {
        path: '/user-center',
        name: 'UserCenter',
        component: () => import('@/views/owner/OwnerDashboard.vue'),
        meta: {
            title: '车主中心',
            requiresAuth: true,
            roles: ['owner']
        }
    },
    {
        path: '/staff-center',
        name: 'StaffCenter',
        component: () => import('@/views/StaffCenter.vue'),
        meta: {
            title: '员工中心',
            requiresAuth: true,
            roles: ['service', 'mechanic', 'warehouse', 'admin']
        }
    },
    {
        path: '/vehicle',
        name: 'Vehicle',
        component: () => import('@/views/VehicleList.vue'),
        meta: {
            title: '车辆管理',
            requiresAuth: true,
            roles: ['owner', 'service']
        }
    },
    {
        path: '/vehicle/history/:id',
        name: 'VehicleHistory',
        component: () => import('@/views/VehicleHistory.vue'),
        meta: {
            title: '维修历史',
            requiresAuth: true
        }
    },
    {
        path: '/appointment/create',
        name: 'AppointmentCreate',
        component: () => import('@/views/owner/AppointmentCreate.vue'),
        meta: {
            title: '创建预约',
            requiresAuth: true,
            roles: ['owner']
        }
    },
    {
        path: '/appointment/list',
        name: 'AppointmentList',
        component: () => import('@/views/owner/AppointmentList.vue'),
        meta: {
            title: '我的预约',
            requiresAuth: true,
            roles: ['owner']
        }
    },
    {
        path: '/placeholder/:type?/:id?',
        name: 'Placeholder',
        component: () => import('@/views/PlaceholderPage.vue'),
        meta: {
            title: '功能开发中'
        }
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/home'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 }
    }
})

// ========== 修改开始：新的 getUserState 函数 ==========
function getUserState() {
    // 1. 优先从 localStorage 获取用户信息
    const localUser = localStorage.getItem('user')
    const localToken = localStorage.getItem('token')

    console.log('路由守卫检查用户状态:')
    console.log('- localStorage.user:', localUser ? '有数据' : '无数据')
    console.log('- localStorage.token:', localToken ? '有数据' : '无数据')

    if (localUser && localToken) {
        try {
            const userData = JSON.parse(localUser)
            console.log('- 从localStorage解析用户数据成功:', {
                id: userData.id,
                no: userData.no,
                role: userData.role,
                name: userData.name
            })
            return {
                isAuthenticated: true,
                userData: userData
            }
        } catch (error) {
            console.error('❌ 解析localStorage用户数据失败:', error)
            // 解析失败，清除无效数据
            localStorage.removeItem('user')
            localStorage.removeItem('token')
        }
    }

    // 2. 如果 localStorage 没有有效数据，从 store 获取
    console.log('- 从store获取用户状态')
    const storeState = store.state.user || {}
    return {
        isAuthenticated: !!storeState.isAuthenticated,
        userData: storeState.userInfo || {}
    }
}
// ========== 修改结束 ==========

// 检查用户是否有权限访问
function hasPermission(userRole, requiredRoles) {
    if (!requiredRoles || requiredRoles.length === 0) return true
    if (!userRole) return false
    return requiredRoles.includes(userRole)
}

// 路由守卫
router.beforeEach((to, from, next) => {
    console.log(`🚀 路由切换: ${from.path} -> ${to.path}`)

    // 设置页面标题
    const defaultTitle = '汽车4S店服务平台'
    if (to.meta.title) {
        document.title = `${to.meta.title} | ${defaultTitle}`
    } else {
        document.title = defaultTitle
    }

    // 获取用户状态
    const userState = getUserState()
    const userRole = userState.userData ? userState.userData.role : null

    console.log('📊 用户状态检查:')
    console.log('- 是否认证:', userState.isAuthenticated)
    console.log('- 用户角色:', userRole)
    console.log('- 目标路由:', to.path)
    console.log('- 需要认证:', to.meta.requiresAuth)
    console.log('- 需要角色:', to.meta.roles)

    // 检查是否需要认证
    if (to.meta.requiresAuth) {
        if (userState.isAuthenticated) {
            // 检查角色权限
            if (hasPermission(userRole, to.meta.roles)) {
                console.log('✅ 权限检查通过，允许访问')
                next()
            } else {
                // 角色不匹配，重定向到对应中心
                console.log('❌ 角色权限不足，用户角色:', userRole, '需要的角色:', to.meta.roles)
                ElMessage.error('您没有权限访问此页面')
                if (userRole === 'owner') {
                    next('/user-center')
                } else {
                    next('/staff-center')
                }
            }
        } else {
            // 未登录，跳转到登录页
            console.log('❌ 未登录，跳转到登录页')
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
        }
    } else if (to.meta.guestOnly && userState.isAuthenticated) {
        // 已登录用户访问游客页面，重定向到对应中心
        console.log('ℹ️ 已登录用户访问游客页面，重定向')
        ElMessage.info('您已登录，将跳转到用户中心')
        if (userRole === 'owner') {
            next('/user-center')
        } else {
            next('/staff-center')
        }
    } else {
        console.log('✅ 无需认证，允许访问')
        next()
    }
})

// 路由后置钩子
router.afterEach(() => {
    console.log('✅ 页面加载完成')
})

export default router