package com.wms.controller;

import com.wms.common.Result;
import com.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理控制器
 * 处理库存相关的接口请求，包括入库、出库、调整、查询等操作
 */
@RestController
@RequestMapping("/api/inventory") // 定义此控制器的基础URL
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * 配件入库
     */
    @PostMapping("/stock-in")
    public Result stockIn(@RequestParam Integer partId,
                          @RequestParam Integer quantity,
                          @RequestParam String operator) {
        return inventoryService.stockIn(partId, quantity, operator);
    }

    /**
     * 配件出库
     */
    @PostMapping("/stock-out")
    public Result stockOut(@RequestParam Integer partId,
                           @RequestParam Integer quantity,
                           @RequestParam String operator,
                           @RequestParam String purpose) {
        return inventoryService.stockOut(partId, quantity, operator, purpose);
    }

    /**
     * 调整库存（手动修改库存数量）
     */
    @PostMapping("/adjust")
    public Result adjustStock(@RequestParam Integer partId,
                              @RequestParam Integer newQuantity,
                              @RequestParam String reason,
                              @RequestParam String operator) {
        return inventoryService.adjustStock(partId, newQuantity, reason, operator);
    }

    /**
     * 获取库存详情（包含配件信息和库存价值）
     */
    @GetMapping("/detail/{partId}")
    public Result getInventoryDetail(@PathVariable Integer partId) {
        return inventoryService.getInventoryDetail(partId);
    }

    /**
     * 获取低库存预警列表
     */
    @GetMapping("/low-stock-warning")
    public Result getLowStockWarning() {
        return inventoryService.getLowStockWarning();
    }

    /**
     * 获取库存统计信息
     */
    @GetMapping("/stats")
    public Result getInventoryStats() {
        return inventoryService.getInventoryStats();
    }
}