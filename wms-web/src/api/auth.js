import request from '@/utils/request'

// 用户登录（所有角色统一登录）
export function login(data) {
    return request({
        url: '/api/user/login',
        method: 'post',
        data: {
            no: data.username,
            password: data.password
        }
    })
}

// 用户注册
export function register(data) {
    return request({
        url: '/api/user/register',
        method: 'post',
        data: {
            no: data.username,
            name: data.name,
            password: data.password,
            phone: data.phone,
            email: data.email,
            role: 'owner' // 注册默认为车主
        }
    })
}

// 获取用户信息
export function getUserInfo(id) {
    return request({
        url: `/api/user/info/${id}`,
        method: 'get'
    })
}

// 退出登录
export function logout() {
    return request({
        url: '/auth/logout', // 你可能需要创建这个接口
        method: 'post'
    })
}

// 创建员工账号（管理员用）
export function createStaff(data) {
    return request({
        url: '/api/user/staff',
        method: 'post',
        data
    })
}

// 获取服务顾问列表
export function getServiceAdvisors() {
    return request({
        url: '/api/user/service-advisors',
        method: 'get'
    })
}

// 获取维修技师列表
export function getMechanics() {
    return request({
        url: '/api/user/mechanics',
        method: 'get'
    })
}