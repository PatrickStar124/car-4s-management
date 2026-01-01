/* eslint-disable no-unused-vars */
import { createRouter, createWebHistory } from 'vue-router'

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
            guest: true
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/RegisterPage.vue'),
        meta: {
            title: '车主注册',
            guest: true
        }
    },
    {
        path: '/user-center',
        name: 'UserCenter',
        component: () => import('@/views/UserCenter.vue'),
        meta: {
            title: '车主中心',
            requiresAuth: true,
            requiredRole: 'owner'
        }
    },
    {
        path: '/staff-center',
        name: 'StaffCenter',
        component: () => import('@/views/StaffCenter.vue'),
        meta: {
            title: '员工中心',
            requiresAuth: true,
            requiredRole: 'staff'
        }
    },
    {
        path: '/vehicle',
        name: 'Vehicle',
        component: () => import('@/views/VehicleList.vue'),
        meta: {
            title: '车辆管理',
            requiresAuth: true,
            requiredRole: 'staff'
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

// 获取用户状态
function getUserState() {
    const user = localStorage.getItem('user')
    const token = localStorage.getItem('token')

    if (user && token) {
        try {
            return {
                isAuthenticated: true,
                userData: JSON.parse(user)
            }
        } catch (e) {
            console.error('解析用户信息失败:', e)
            return {
                isAuthenticated: false,
                userData: null
            }
        }
    }

    return {
        isAuthenticated: false,
        userData: null
    }
}

// 路由守卫
router.beforeEach((to, from, next) => {
    console.log(`路由切换: ${from.path} -> ${to.path}`)

    // 设置页面标题
    const defaultTitle = '汽车4S店服务平台'
    if (to.meta.title) {
        document.title = `${to.meta.title} | ${defaultTitle}`
    } else {
        document.title = defaultTitle
    }

    // 获取用户状态
    const userState = getUserState()

    // 检查是否需要认证
    if (to.meta.requiresAuth) {
        if (userState.isAuthenticated) {
            const userRole = userState.userData.role
            const requiredRole = to.meta.requiredRole

            // 检查角色权限
            if (!requiredRole) {
                next()
            } else if (requiredRole === 'owner' && userRole === 'owner') {
                next()
            } else if (requiredRole === 'staff' && userRole !== 'owner') {
                next()
            } else {
                // 角色不匹配，重定向到对应中心
                if (userRole === 'owner') {
                    next('/user-center')
                } else {
                    next('/staff-center')
                }
            }
        } else {
            // 未登录，跳转到登录页
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
        }
    } else if (to.meta.guest && userState.isAuthenticated) {
        // 已登录用户访问游客页面，重定向
        const userRole = userState.userData.role
        if (userRole === 'owner') {
            next('/user-center')
        } else {
            next('/staff-center')
        }
    } else {
        next()
    }
})

// 路由后置钩子
router.afterEach(() => {
    console.log('✅ 页面加载完成')
})

export default router