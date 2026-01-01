import request from '../utils/request'

/**
 * 用户管理API
 */

// 用户登录
export function login(data) {
    return request({
        url: '/api/user/login',
        method: 'post',
        data: data
    })
}

// 用户注册
export function register(user) {
    return request({
        url: '/api/user/register',
        method: 'post',
        data: user
    })
}

// 获取用户信息
export function getUserInfo(id) {
    return request({
        url: `/api/user/info/${id}`,
        method: 'get'
    })
}

// 根据角色获取用户列表
export function getUsersByRole(role) {
    return request({
        url: `/api/user/role/${role}`,
        method: 'get'
    })
}

// 创建员工账号（管理员用）
export function createStaff(user) {
    return request({
        url: '/api/user/staff',
        method: 'post',
        data: user
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

// 获取所有用户列表
export function getUserList() {
    return request({
        url: '/api/user/list',
        method: 'get'
    })
}

// 更新用户信息
export function updateUser(user) {
    return request({
        url: '/api/user/update',
        method: 'put',
        data: user
    })
}

// 删除用户
export function deleteUser(id) {
    return request({
        url: `/api/user/${id}`,
        method: 'delete'
    })
}

// 更新用户角色
export function updateUserRole(userId, role) {
    return request({
        url: `/api/user/role/${userId}`,
        method: 'put',
        params: { role }
    })
}

// 导出所有API为一个对象（可选，方便统一导入）
export default {
    login,
    register,
    getUserInfo,
    getUsersByRole,
    createStaff,
    getServiceAdvisors,
    getMechanics,
    getUserList,
    updateUser,
    deleteUser,
    updateUserRole
}