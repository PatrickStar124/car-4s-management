import request from '@/utils/request'

export default {
    // 创建预约
    createAppointment(data) {
        return request({
            url: '/api/appointment/create',
            method: 'post',
            data: data
        })
    },

    // 检查时间冲突
    checkTimeConflict(vehicleId, startTime, endTime) {
        return request({
            url: '/api/appointment/check-conflict',
            method: 'post',
            params: {
                vehicleId,
                startTime,
                endTime
            }
        })
    },

    // 取消预约
    cancelAppointment(appointmentId, userId) {
        return request({
            url: `/api/appointment/cancel/${appointmentId}`,
            method: 'put',
            params: { userId }
        })
    },

    // 确认预约
    confirmAppointment(appointmentId, serviceAdvisorId) {
        return request({
            url: `/api/appointment/confirm/${appointmentId}`,
            method: 'put',
            params: { serviceAdvisorId }
        })
    },

    // 根据车主查询预约
    getAppointmentsByOwner(ownerId) {
        return request({
            url: `/api/appointment/owner/${ownerId}`,
            method: 'get'
        })
    },

    // 根据服务顾问查询预约
    getAppointmentsByAdvisor(advisorId) {
        return request({
            url: `/api/appointment/advisor/${advisorId}`,
            method: 'get'
        })
    },

    // 根据状态查询预约
    getAppointmentsByStatus(status) {
        return request({
            url: `/api/appointment/status/${status}`,
            method: 'get'
        })
    },

    // 获取今日预约
    getTodayAppointments() {
        return request({
            url: '/api/appointment/today',
            method: 'get'
        })
    },

    // 获取所有预约
    getAppointmentList() {
        return request({
            url: '/api/appointment/list',
            method: 'get'
        })
    }
}