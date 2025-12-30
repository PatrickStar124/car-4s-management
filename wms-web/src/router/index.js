import { createRouter, createWebHistory } from 'vue-router'

// 定义路由表，所有组件从 views/ 目录懒加载
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
        meta: { title: '首页' }
    },
    // 图书列表页
    {
        path: '/booklist',
        name: 'BookList',
        component: () => import('@/views/BookList.vue'),
        meta: { title: '全部图书' }
    },
    // 新增书籍页面（仅登录可访问，删除管理员限制）
    {
        path: '/bookadd',
        name: 'BookAdd',
        component: () => import('@/views/BookAdd.vue'),
        meta: {
            title: '新增书籍',
            requiresAuth: true
        }
    },
    // 购物车页面
    {
        path: '/cart',
        name: 'Cart',
        component: () => import('@/views/CartPage.vue'),
        meta: { title: '购物车', requiresAuth: true }
    },
    // 用户中心页面
    {
        path: '/usercenter',
        name: 'UserCenter',
        component: () => import('@/views/UserCenter.vue'),
        meta: { title: '个人中心', requiresAuth: true }
    },
    // 登录页面
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/LoginPage.vue'),
        meta: { title: '登录' }
    },
    // 注册页面
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/RegisterPage.vue'),
        meta: { title: '注册' }
    },
    // 404 页面处理：重定向到首页
    {
        path: '/:pathMatch(.*)*',
        redirect: '/home'
    }
]

// 创建路由实例
const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

// 用户状态管理函数
function getUserFromLocalStorage() {
    const userStr = localStorage.getItem('user')
    if (userStr) {
        try {
            return JSON.parse(userStr)
        } catch (e) {
            console.error('解析用户信息失败:', e)
            localStorage.removeItem('user')
            return null
        }
    }
    return null
}

// 全局前置路由守卫
router.beforeEach((to, from, next) => {
    // 1. 设置页面标题
    if (to.meta.title) {
        document.title = `${to.meta.title} - 图书购物车系统`
    }

    // 2. 检查用户登录状态
    const user = getUserFromLocalStorage()

    // 3. 验证需要登录的页面
    if (to.meta.requiresAuth && !user) {
        console.log('访问需要登录的页面，但用户未登录，跳转到登录页')
        alert('请先登录后再操作！')
        next({
            path: '/login',
            query: { redirect: to.fullPath }
        })
        return
    }

    // 4. 如果已登录且访问登录/注册页，跳转到首页
    if (user && (to.path === '/login' || to.path === '/register')) {
        console.log('已登录用户访问登录/注册页，跳转到首页')
        next({ path: '/home' })
        return
    }

    // 5. 所有验证通过，放行
    next()
})

// 全局后置钩子
router.afterEach((to, from) => {
    console.log(`路由切换: ${from.path} -> ${to.path}`)
})

export default router