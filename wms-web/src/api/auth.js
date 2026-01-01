import request from '@/utils/request'

export default {
    // 登录 - 修改为使用 params（URL参数）
    login(data) {
        return request({
            url: '/api/user/login',
            method: 'post',
            // 关键修改：使用 params 而不是 data
            params: {
                no: data.username || data.no,  // 兼容两种参数名
                password: data.password
            }
        })
    },

    // 注册 - 保持 data（JSON请求体）
    register(data) {
        return request({
            url: '/api/user/register',
            method: 'post',
            data: data  // 这是正确的，因为后端使用 @RequestBody
        })
    },

    // 获取用户信息
    getUserInfo(id) {
        return request({
            url: `/api/user/info/${id}`,
            method: 'get'
        })
    },

    // 根据角色获取用户
    getUsersByRole(role) {
        return request({
            url: `/api/user/role/${role}`,
            method: 'get'
        })
    },

    // 创建员工账号 - 保持 data（JSON请求体）
    createStaff(data) {
        return request({
            url: '/api/user/staff',
            method: 'post',
            data: data  // 这是正确的，因为后端使用 @RequestBody
        })
    },

    // 更新用户信息 - 保持 data（JSON请求体）
    updateUser(data) {
        return request({
            url: '/api/user/update',
            method: 'put',
            data: data  // 这是正确的，因为后端使用 @RequestBody
        })
    },

    // 删除用户
    deleteUser(id) {
        return request({
            url: `/api/user/${id}`,
            method: 'delete'
        })
    },

    // 获取服务顾问列表
    getServiceAdvisors() {
        return request({
            url: '/api/user/service-advisors',
            method: 'get'
        })
    },

    // 获取维修技师列表
    getMechanics() {
        return request({
            url: '/api/user/mechanics',
            method: 'get'
        })
    },

    // 获取所有用户
    getUserList() {
        return request({
            url: '/api/user/list',
            method: 'get'
        })
    }
}