import request from '@/utils/request'

export default {
    // 创建维修工单
    createOrder(data) {
        return request({
            url: '/api/repair-order/create',
            method: 'post',
            data: data
        })
    },

    // 更新工单状态
    updateOrderStatus(orderId, status, operatorId) {
        return request({
            url: `/api/repair-order/status/${orderId}`,
            method: 'put',
            params: {
                status,
                operatorId
            }
        })
    },

    // 分配维修技师
    assignMechanic(orderId, mechanicId) {
        return request({
            url: `/api/repair-order/assign-mechanic/${orderId}`,
            method: 'put',
            params: { mechanicId }
        })
    },

    // 添加工单项
    addOrderItem(orderId, itemData) {
        return request({
            url: `/api/repair-order/add-item/${orderId}`,
            method: 'post',
            params: itemData
        })
    },

    // 计算工单总金额
    calculateOrderAmount(orderId) {
        return request({
            url: `/api/repair-order/calculate/${orderId}`,
            method: 'get'
        })
    },

    // 根据车主查询工单
    getOrdersByOwner(ownerId) {
        return request({
            url: `/api/repair-order/owner/${ownerId}`,
            method: 'get'
        })
    },

    // 根据车辆查询工单
    getOrdersByVehicle(vehicleId) {
        return request({
            url: `/api/repair-order/vehicle/${vehicleId}`,
            method: 'get'
        })
    },

    // 根据状态查询工单
    getOrdersByStatus(status) {
        return request({
            url: `/api/repair-order/status/${status}`,
            method: 'get'
        })
    },

    // 获取工单详情
    getOrderDetail(orderId) {
        return request({
            url: `/api/repair-order/detail/${orderId}`,
            method: 'get'
        })
    },

    // 提交报价
    submitQuote(orderId, estimatedAmount, remark) {
        return request({
            url: `/api/repair-order/submit-quote/${orderId}`,
            method: 'post',
            params: {
                estimatedAmount,
                remark
            }
        })
    },

    // 完成工单
    completeOrder(orderId, actualAmount) {
        return request({
            url: `/api/repair-order/complete/${orderId}`,
            method: 'post',
            params: { actualAmount }
        })
    },

    // 获取所有工单
    getOrderList() {
        return request({
            url: '/api/repair-order/list',
            method: 'get'
        })
    }
}