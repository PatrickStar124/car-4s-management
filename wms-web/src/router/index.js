import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
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
    // =================== 新增：添加车辆的路由 ===================
    {
        path: '/vehicle/create',
        name: 'VehicleCreate',
        component: () => import('@/views/owner/VehicleCreate.vue'),
        meta: {
            title: '添加车辆',
            requiresAuth: true,
            roles: ['owner'] // 只有车主可以添加自己的车辆
        }
    },
    // ==========================================================
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

// ========== 修正：更可靠的用户状态检查函数 ==========
function getUserState() {
    // 1. 优先从 localStorage 获取用户信息
    const localUser = localStorage.getItem('user')
    const localToken = localStorage.getItem('token')

    // 如果 localStorage 中有用户和token，则认为用户已登录
    if (localUser && localToken) {
        try {
            const userData = JSON.parse(localUser)
            return {
                isAuthenticated: true,
                userData: userData
            }
        } catch (error) {
            console.error('❌ 解析localStorage用户数据失败，将清除无效数据:', error)
            localStorage.removeItem('user')
            localStorage.removeItem('token')
        }
    }

    // 2. 如果 localStorage 没有有效数据，则认为用户未登录
    return {
        isAuthenticated: false,
        userData: null
    }
}
// ========== 修正结束 ==========

// 检查用户是否有权限访问
function hasPermission(userRole, requiredRoles) {
    if (!requiredRoles || requiredRoles.length === 0) return true
    if (!userRole) return false
    return requiredRoles.includes(userRole)
}

// 路由守卫
router.beforeEach((to, from, next) => {
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

    // 检查是否需要认证
    if (to.meta.requiresAuth) {
        if (userState.isAuthenticated) {
            // 检查角色权限
            if (hasPermission(userRole, to.meta.roles)) {
                next()
            } else {
                ElMessage.error('您没有权限访问此页面')
                if (userRole === 'owner') {
                    next('/user-center')
                } else {
                    next('/staff-center')
                }
            }
        } else {
            ElMessage.warning('请先登录')
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
        }
    } else if (to.meta.guestOnly && userState.isAuthenticated) {
        ElMessage.info('您已登录，将跳转到用户中心')
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
    // 可以在这里添加页面加载完成后的逻辑
})

export default router