// InventoryService.java
package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.common.Result;
import com.wms.entity.Inventory;

import java.math.BigDecimal;

public interface InventoryService extends IService<Inventory> {

    /**
     * 入库操作
     */
    Result stockIn(Integer partId, Integer quantity, String operator);

    /**
     * 出库操作
     */
    Result stockOut(Integer partId, Integer quantity, String operator, String purpose);

    /**
     * 调整库存
     */
    Result adjustStock(Integer partId, Integer newQuantity, String reason, String operator);

    /**
     * 获取库存详情（包含配件信息）
     */
    Result getInventoryDetail(Integer partId);

    /**
     * 获取低库存预警列表（库存低于安全库存）
     */
    Result getLowStockWarning();

    /**
     * 获取库存统计
     */
    Result getInventoryStats();
}