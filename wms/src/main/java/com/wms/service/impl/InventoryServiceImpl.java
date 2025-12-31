// InventoryServiceImpl.java
package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.Result;
import com.wms.entity.Inventory;
import com.wms.entity.Part;
import com.wms.mapper.InventoryMapper;
import com.wms.mapper.PartMapper;
import com.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private PartMapper partMapper;

    // 配件入库
    @Override
    @Transactional
    public Result stockIn(Integer partId, Integer quantity, String operator) {
        try {
            if (partId == null || quantity == null || quantity <= 0) {
                return Result.fail("参数错误");
            }

            // 1. 验证配件是否存在
            Part part = partMapper.selectById(partId);
            if (part == null) {
                return Result.fail("配件不存在");
            }

            // 2. 获取库存记录
            LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Inventory::getPartId, partId);
            Inventory inventory = this.getOne(wrapper);

            if (inventory == null) {
                // 如果没有库存记录，创建新的
                inventory = new Inventory();
                inventory.setPartId(partId);
                inventory.setStockQuantity(quantity);
                inventory.setSafeStock(10);
                inventory.setLockedQuantity(0);
                inventory.setLastInboundTime(LocalDateTime.now());
                this.save(inventory);
            } else {
                // 更新现有库存
                inventory.setStockQuantity(inventory.getStockQuantity() + quantity);
                inventory.setLastInboundTime(LocalDateTime.now());
                this.updateById(inventory);
            }

            // 3. 记录入库日志（这里可以扩展记录到专门的日志表）
            System.out.println(String.format("入库记录：配件ID=%d，数量=%d，操作员=%s，时间=%s",
                    partId, quantity, operator, LocalDateTime.now()));

            return Result.suc("入库成功", inventory);
        } catch (Exception e) {
            return Result.fail("入库失败: " + e.getMessage());
        }
    }

    // 配件出库
    @Override
    @Transactional
    public Result stockOut(Integer partId, Integer quantity, String operator, String purpose) {
        try {
            if (partId == null || quantity == null || quantity <= 0) {
                return Result.fail("参数错误");
            }

            // 1. 验证配件是否存在
            Part part = partMapper.selectById(partId);
            if (part == null) {
                return Result.fail("配件不存在");
            }

            // 2. 获取库存记录
            LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Inventory::getPartId, partId);
            Inventory inventory = this.getOne(wrapper);

            if (inventory == null) {
                return Result.fail("该配件无库存记录");
            }

            // 3. 检查库存是否充足（考虑锁定数量）
            int availableStock = inventory.getStockQuantity() - inventory.getLockedQuantity();
            if (availableStock < quantity) {
                return Result.fail(String.format("库存不足，可用数量：%d", availableStock));
            }

            // 4. 扣减库存
            inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
            inventory.setLastOutboundTime(LocalDateTime.now());
            this.updateById(inventory);

            // 5. 记录出库日志
            System.out.println(String.format("出库记录：配件ID=%d，数量=%d，操作员=%s，用途=%s，时间=%s",
                    partId, quantity, operator, purpose, LocalDateTime.now()));

            return Result.suc("出库成功", inventory);
        } catch (Exception e) {
            return Result.fail("出库失败: " + e.getMessage());
        }
    }

    // 调整库存（手动修改库存数量）
    @Override
    @Transactional
    public Result adjustStock(Integer partId, Integer newQuantity, String reason, String operator) {
        try {
            if (partId == null || newQuantity == null || newQuantity < 0) {
                return Result.fail("参数错误");
            }

            // 1. 验证配件
            Part part = partMapper.selectById(partId);
            if (part == null) {
                return Result.fail("配件不存在");
            }

            // 2. 获取库存记录
            LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Inventory::getPartId, partId);
            Inventory inventory = this.getOne(wrapper);

            if (inventory == null) {
                inventory = new Inventory();
                inventory.setPartId(partId);
                inventory.setStockQuantity(newQuantity);
                inventory.setSafeStock(10);
                inventory.setLockedQuantity(0);
                this.save(inventory);
            } else {
                // 记录调整前的数量
                int oldQuantity = inventory.getStockQuantity();
                inventory.setStockQuantity(newQuantity);
                this.updateById(inventory);

                // 记录调整日志
                System.out.println(String.format("库存调整：配件ID=%d，原数量=%d，新数量=%d，原因=%s，操作员=%s",
                        partId, oldQuantity, newQuantity, reason, operator));
            }

            return Result.suc("库存调整成功", inventory);
        } catch (Exception e) {
            return Result.fail("库存调整失败: " + e.getMessage());
        }
    }

    // 获取库存详情（包含配件信息和库存价值）
    @Override
    public Result getInventoryDetail(Integer partId) {
        try {
            // 1. 获取配件信息
            Part part = partMapper.selectById(partId);
            if (part == null) {
                return Result.fail("配件不存在");
            }

            // 2. 获取库存信息
            LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Inventory::getPartId, partId);
            Inventory inventory = this.getOne(wrapper);

            if (inventory == null) {
                // 如果没有库存记录，返回默认值
                inventory = new Inventory();
                inventory.setPartId(partId);
                inventory.setStockQuantity(0);
                inventory.setSafeStock(10);
                inventory.setLockedQuantity(0);
            }

            // 3. 计算库存价值
            BigDecimal stockValue = part.getPurchasePrice() != null ?
                    part.getPurchasePrice().multiply(BigDecimal.valueOf(inventory.getStockQuantity())) :
                    BigDecimal.ZERO;

            // 4. 返回完整信息
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("part", part);
            result.put("inventory", inventory);
            result.put("stockValue", stockValue);
            result.put("isLowStock", inventory.getStockQuantity() < inventory.getSafeStock());

            return Result.suc(result);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    // 获取低库存预警列表
    @Override
    public Result getLowStockWarning() {
        try {
            LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(Inventory::getStockQuantity, com.baomidou.mybatisplus.core.toolkit.Wrappers.<Inventory>lambdaQuery()
                    .getSqlSelect() + ".safe_stock"); // 库存数量 < 安全库存

            List<Inventory> lowStockList = this.list(wrapper);

            // 关联配件信息
            List<java.util.Map<String, Object>> resultList = lowStockList.stream().map(inventory -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("inventory", inventory);

                Part part = partMapper.selectById(inventory.getPartId());
                map.put("part", part);
                map.put("shortage", inventory.getSafeStock() - inventory.getStockQuantity()); // 短缺数量

                return map;
            }).collect(Collectors.toList());

            return Result.suc(resultList);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    // 获取库存统计信息
    @Override
    public Result getInventoryStats() {
        try {
            // 1. 统计库存总价值
            List<Inventory> allInventory = this.list();
            BigDecimal totalValue = BigDecimal.ZERO;
            int totalItems = 0;
            int totalQuantity = 0;
            int lowStockCount = 0;

            for (Inventory inventory : allInventory) {
                Part part = partMapper.selectById(inventory.getPartId());
                if (part != null && part.getPurchasePrice() != null) {
                    totalValue = totalValue.add(
                            part.getPurchasePrice().multiply(BigDecimal.valueOf(inventory.getStockQuantity()))
                    );
                }
                totalItems++;
                totalQuantity += inventory.getStockQuantity();

                if (inventory.getStockQuantity() < inventory.getSafeStock()) {
                    lowStockCount++;
                }
            }

            // 2. 返回统计数据
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalItems", totalItems);           // 配件种类数
            stats.put("totalQuantity", totalQuantity);     // 总库存数量
            stats.put("totalValue", totalValue);           // 总库存价值
            stats.put("lowStockCount", lowStockCount);     // 低库存种类数
            stats.put("avgStockValue", totalItems > 0 ?
                    totalValue.divide(BigDecimal.valueOf(totalItems), 2, BigDecimal.ROUND_HALF_UP) :
                    BigDecimal.ZERO);                          // 平均库存价值

            return Result.suc(stats);
        } catch (Exception e) {
            return Result.fail("统计失败: " + e.getMessage());
        }
    }
}