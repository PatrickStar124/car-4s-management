package com.wms.controller;

import com.wms.common.Result;
import com.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理控制器
 * 处理库存相关的接口请求，包括入库、出库、调整、查询等操作
 * 接口排序逻辑：基础查询 → 核心操作 → 特殊查询
 */
@RestController
@RequestMapping("/api/inventory") // 定义此控制器的基础URL
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // ======================== 基础查询接口（通用/全量/详情）========================
    /**
     * 获取所有库存列表
     * 用途：管理员后台查看系统内所有配件的库存基础数据
     * @return 全量库存信息列表
     */
    @GetMapping("/list")
    public Result getInventoryList() {
        return inventoryService.getAllInventory();
    }

    /**
     * 获取库存详情（包含配件信息和库存价值）
     * 用途：查看单个配件的库存明细（数量、入库时间、价值等）
     * @param partId 配件ID
     * @return 该配件的库存详情信息
     */
    @GetMapping("/detail/{partId}")
    public Result getInventoryDetail(@PathVariable Integer partId) {
        return inventoryService.getInventoryDetail(partId);
    }

    // ======================== 核心操作接口（入库/出库/调整）========================
    /**
     * 配件入库
     * 用途：采购配件到货后，录入系统增加对应配件库存数量
     * @param partId 配件ID
     * @param quantity 入库数量
     * @param operator 操作人（如仓库管理员账号/姓名）
     * @return 入库操作结果（成功/失败+提示信息）
     */
    @PostMapping("/stock-in")
    public Result stockIn(@RequestParam Integer partId,
                          @RequestParam Integer quantity,
                          @RequestParam String operator) {
        return inventoryService.stockIn(partId, quantity, operator);
    }

    /**
     * 配件出库
     * 用途：维修领用、报废、调拨等场景，减少对应配件库存数量
     * @param partId 配件ID
     * @param quantity 出库数量
     * @param operator 操作人（如仓库管理员账号/姓名）
     * @param purpose 出库用途（如：维修领用、报废处理、库存调拨）
     * @return 出库操作结果（成功/失败+提示信息）
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
     * 用途：盘点后库存数量不符、系统数据错误等场景，手动修正库存数量
     * @param partId 配件ID
     * @param newQuantity 调整后的库存数量
     * @param reason 调整原因（如：盘点差异、系统纠错、损耗核销）
     * @param operator 操作人（如仓库管理员账号/姓名）
     * @return 库存调整结果（成功/失败+提示信息）
     */
    @PostMapping("/adjust")
    public Result adjustStock(@RequestParam Integer partId,
                              @RequestParam Integer newQuantity,
                              @RequestParam String reason,
                              @RequestParam String operator) {
        return inventoryService.adjustStock(partId, newQuantity, reason, operator);
    }

    // ======================== 特殊查询接口（预警/统计）========================
    /**
     * 获取低库存预警列表
     * 用途：仓库管理端查看库存低于安全阈值的配件，提醒及时采购
     * @return 低库存预警配件列表（包含配件ID、当前库存、安全库存等）
     */
    @GetMapping("/low-stock-warning")
    public Result getLowStockWarning() {
        return inventoryService.getLowStockWarning();
    }

    /**
     * 获取库存统计信息
     * 用途：管理层查看库存整体情况，如总库存价值、各品类占比、出入库频次等
     * @return 库存统计汇总数据（总数量、总价值、品类分布等）
     */
    @GetMapping("/stats")
    public Result getInventoryStats() {
        return inventoryService.getInventoryStats();
    }
}