// src/api/vehicle.js
import request from '@/utils/request'

const baseUrl = '/api/vehicle'

// 获取车辆列表
export function getVehicleList() {
    return request({
        url: `${baseUrl}/list`,
        method: 'get'
    })
}

// 根据车主ID获取车辆
export function getVehiclesByOwner(ownerId) {
    return request({
        url: `${baseUrl}/owner/${ownerId}`,
        method: 'get'
    })
}

// 添加车辆
export function addVehicle(data) {
    return request({
        url: `${baseUrl}/add`,
        method: 'post',
        data
    })
}

// 更新车辆
export function updateVehicle(data) {
    return request({
        url: `${baseUrl}/update`,
        method: 'put',
        data
    })
}

// 获取车辆详情
export function getVehicleDetail(id) {
    return request({
        url: `${baseUrl}/detail/${id}`,
        method: 'get'
    })
}

// 获取车辆维修历史
export function getVehicleRepairHistory(vehicleId) {
    return request({
        url: `${baseUrl}/history/${vehicleId}`,
        method: 'get'
    })
}

// 根据车牌号查询
export function getVehicleByPlate(plateNumber) {
    return request({
        url: `${baseUrl}/plate/${plateNumber}`,
        method: 'get'
    })
}

// 删除车辆
export function deleteVehicle(id) {
    return request({
        url: `${baseUrl}/${id}`,
        method: 'delete'
    })
}