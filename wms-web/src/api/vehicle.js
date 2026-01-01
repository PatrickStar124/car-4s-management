import request from '@/utils/request'

export default {
    // 添加车辆
    addVehicle(data) {
        return request({
            url: '/api/vehicle/add',
            method: 'post',
            data: data
        })
    },

    // 更新车辆
    updateVehicle(data) {
        return request({
            url: '/api/vehicle/update',
            method: 'put',
            data: data
        })
    },

    // 根据车主查询车辆
    getVehiclesByOwner(ownerId) {
        return request({
            url: `/api/vehicle/owner/${ownerId}`,
            method: 'get'
        })
    },

    // 根据车牌查询车辆
    getVehicleByPlate(plateNumber) {
        return request({
            url: `/api/vehicle/plate/${plateNumber}`,
            method: 'get'
        })
    },

    // 获取车辆详情
    getVehicleDetail(id) {
        return request({
            url: `/api/vehicle/detail/${id}`,
            method: 'get'
        })
    },

    // 获取维修历史
    getVehicleRepairHistory(vehicleId) {
        return request({
            url: `/api/vehicle/history/${vehicleId}`,
            method: 'get'
        })
    },

    // 获取所有车辆
    getVehicleList() {
        return request({
            url: '/api/vehicle/list',
            method: 'get'
        })
    },

    // 删除车辆
    deleteVehicle(id) {
        return request({
            url: `/api/vehicle/${id}`,
            method: 'delete'
        })
    }
}