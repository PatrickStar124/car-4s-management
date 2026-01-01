// src/api/auth.js
import request from '@/utils/request'

/**
 * 用户登录接口
 * @param {Object} data 登录数据
 * @param {string} data.username 用户名/工号
 * @param {string} data.password 密码
 * @returns {Promise} Promise对象
 */
export function login(data) {
    console.log('登录请求参数:', data)
    return request({
        url: '/api/user/login',
        method: 'post',
        params: {  // 使用 params 传递查询参数
            no: data.username,      // 对应后端的 @RequestParam String no
            password: data.password // 对应后端的 @RequestParam String password
        }
    })
}

/**
 * 用户注册接口（车主注册）
 * @param {Object} data 注册数据
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @param {string} data.name 姓名
 * @param {string} data.phone 电话
 * @param {string} data.email 邮箱
 * @returns {Promise} Promise对象
 */
export function register(data) {
    return request({
        url: '/api/user/register',
        method: 'post',
        data: {  // 使用 data 传递JSON请求体
            no: data.username,
            name: data.name,
            password: data.password,
            phone: data.phone,
            email: data.email,
            role: 'owner' // 注册默认为车主
        }
    })
}

/**
 * 获取用户信息
 * @param {number} id 用户ID
 * @returns {Promise} Promise对象
 */
export function getUserInfo(id) {
    return request({
        url: `/api/user/info/${id}`,
        method: 'get'
    })
}

/**
 * 退出登录
 * @returns {Promise} Promise对象
 */
export function logout() {
    return request({
        url: '/api/user/logout',
        method: 'post'
    })
}

/**
 * 创建员工账号（管理员功能）
 * @param {Object} data 员工数据
 * @param {string} data.no 工号
 * @param {string} data.name 姓名
 * @param {string} data.password 密码
 * @param {string} data.phone 电话
 * @param {string} data.email 邮箱
 * @param {string} data.role 角色
 * @returns {Promise} Promise对象
 */
export function createStaff(data) {
    return request({
        url: '/api/user/staff',
        method: 'post',
        data: {
            no: data.no,
            name: data.name,
            password: data.password,
            phone: data.phone,
            email: data.email,
            role: data.role
        }
    })
}

/**
 * 获取服务顾问列表
 * @returns {Promise} Promise对象
 */
export function getServiceAdvisors() {
    return request({
        url: '/api/user/service-advisors',
        method: 'get'
    })
}

/**
 * 获取维修技师列表
 * @returns {Promise} Promise对象
 */
export function getMechanics() {
    return request({
        url: '/api/user/mechanics',
        method: 'get'
    })
}

/**
 * 根据角色获取用户列表
 * @param {string} role 用户角色
 * @returns {Promise} Promise对象
 */
export function getUsersByRole(role) {
    return request({
        url: `/api/user/role/${role}`,
        method: 'get'
    })
}

/**
 * 更新用户信息
 * @param {Object} data 用户数据
 * @param {number} data.id 用户ID
 * @param {string} data.name 姓名
 * @param {string} data.phone 电话
 * @param {string} data.email 邮箱
 * @returns {Promise} Promise对象
 */
export function updateUser(data) {
    return request({
        url: '/api/user/update',
        method: 'put',
        data: {
            id: data.id,
            name: data.name,
            phone: data.phone,
            email: data.email
        }
    })
}

/**
 * 删除用户（逻辑删除）
 * @param {number} id 用户ID
 * @returns {Promise} Promise对象
 */
export function deleteUser(id) {
    return request({
        url: `/api/user/${id}`,
        method: 'delete'
    })
}

/**
 * 获取所有用户列表
 * @returns {Promise} Promise对象
 */
export function getUserList() {
    return request({
        url: '/api/user/list',
        method: 'get'
    })
}