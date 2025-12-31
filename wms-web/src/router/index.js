import { createRouter, createWebHistory } from 'vue-router'

// 路由定义
const routes = [
    // 默认根路径重定向到首页
    {
        path: '/',
        redirect: '/home'
    },
    // 首页
    {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/HomePage.vue'),
        meta: {
            title: '首页'
        }
    },
    // 统一登录页面
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/LoginPage.vue'),
        meta: {
            title: '登录',
            guest: true // 游客可访问
        }
    },
    // 车主注册页面
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/RegisterPage.vue'),
        meta: {
            title: '车主注册',
            guest: true // 游客可访问
        }
    },
    // 车主中心页面
    {
        path: '/user-center',
        name: 'UserCenter',
        component: () => import('@/views/UserCenter.vue'),
        meta: {
            title: '车主中心',
            requiresAuth: true, // 需要登录
            requiredRole: 'owner' // 需要 'owner' 角色
        }
    },
    // 员工中心页面
    {
        path: '/staff-center',
        name: 'StaffCenter',
        component: () => import('@/views/StaffCenter.vue'),
        meta: {
            title: '员工中心',
            requiresAuth: true, // 需要登录
            requiredRole: 'staff' // 需要 'staff' 角色
        }
    },
    // 404 页面：修正为重定向到首页，因为你没有 NotFound.vue 文件
    {
        path: '/:pathMatch(.*)*',
        redirect: '/home'
    }
]

// 创建路由实例
const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
    // eslint-disable-next-line no-unused-vars
    scrollBehavior(to, from, savedPosition) {
        // 始终滚动到顶部
        return { top: 0 }
    }
})

/**
 * 从 localStorage 获取用户状态
 * @returns {Object} 用户状态对象
 */
function getUserState() {
    const userInfoStr = localStorage.getItem('user')
    const token = localStorage.getItem('token')

    if (userInfoStr && token) {
        try {
            const userInfo = JSON.parse(userInfoStr)
            return {
                isAuthenticated: true,
                userData: userInfo
            }
        } catch (e) {
            console.error('解析用户信息失败:', e)
            localStorage.removeItem('user')
            localStorage.removeItem('token')
        }
    }

    return {
        isAuthenticated: false,
        userData: null
    }
}

/**
 * 设置页面标题
 */
function setPageTitle(to) {
    const defaultTitle = '汽车4S店服务平台'
    if (to.meta.title) {
        document.title = `${to.meta.title} | ${defaultTitle}`
    } else {
        document.title = defaultTitle
    }
}

// 全局前置路由守卫
// eslint-disable-next-line no-unused-vars
router.beforeEach((to, from, next) => {
    console.log(`路由切换: ${from.fullPath} -> ${to.fullPath}`)

    // 1. 设置页面标题
    setPageTitle(to)

    // 2. 获取用户状态
    const userState = getUserState()

    // 3. 检查路由权限
    if (to.meta.requiresAuth) {
        if (userState.isAuthenticated) {
            const userRole = userState.userData.role
            const requiredRole = to.meta.requiredRole

            if ((requiredRole === 'owner' && userRole === 'owner') ||
                (requiredRole === 'staff' && userRole !== 'owner')) {
                next()
            } else {
                console.warn(`角色不匹配，无法访问 ${to.path}`)
                next('/home')
            }
        } else {
            console.log('用户未登录，跳转到登录页')
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
        }
    } else if (to.meta.guest && userState.isAuthenticated) {
        console.log('已登录用户访问游客页，重定向到主页')
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

// 全局后置钩子
// eslint-disable-next-line no-unused-vars
router.afterEach((to, from) => {
    console.log(`✅ 成功进入页面: ${to.name || to.path}`)
})

export default router