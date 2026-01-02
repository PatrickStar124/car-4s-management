// src/api/inventory.js

import request from '@/utils/request'

export default {
    /**
     * 配件入库
     * @param {Object} data - { partId, quantity, operator }
     */
    stockIn(data) {
        return request({
            url: '/api/inventory/stock-in',
            method: 'post',
            params: data
        })
    },

    /**
     * 配件出库
     * @param {Object} data - { partId, quantity, operator, purpose }
     */
    stockOut(data) {
        return request({
            url: '/api/inventory/stock-out',
            method: 'post',
            params: data
        })
    },

    /**
     * 调整库存
     * @param {Object} data - { partId, newQuantity, reason, operator }
     */
    adjustStock(data) {
        return request({
            url: '/api/inventory/adjust',
            method: 'post',
            params: data
        })
    },

    /**
     * 获取库存详情
     * @param {number} partId - 配件ID
     */
    getInventoryDetail(partId) {
        return request({
            url: `/api/inventory/detail/${partId}`,
            method: 'get'
        })
    },

    /**
     * 获取低库存预警列表
     */
    getLowStockWarning() {
        return request({
            url: '/api/inventory/low-stock-warning',
            method: 'get'
        })
    },

    /**
     * 获取库存统计信息
     */
    getInventoryStats() {
        return request({
            url: '/api/inventory/stats',
            method: 'get'
        })
    },

    /**
     * 获取所有库存列表
     */
    getAllInventory() {
        return request({
            url: '/api/inventory/list',
            method: 'get'
        })
    }
}