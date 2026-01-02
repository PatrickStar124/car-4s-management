package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.Result;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder>
        implements RepairOrderService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PartMapper partMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    // 工单状态常量
    private static final String STATUS_PENDING = "pending";        // 待接车
    private static final String STATUS_INSPECTING = "inspecting";  // 检查中
    private static final String STATUS_QUOTE_PENDING = "quote_pending"; // 待确认报价
    private static final String STATUS_REPAIRING = "repairing";    // 维修中
    private static final String STATUS_COMPLETED = "completed";    // 已完成
    private static final String STATUS_DELIVERED = "delivered";    // 已交车

    // 补回createOrder方法（已添加）
    @Override
    @Transactional
    public Result createOrder(RepairOrder order) {
        // ... 你的createOrder实现代码（保持不变）
    }

    // ✅ 补回updateOrderStatus方法（解决当前报错）
    @Override
    @Transactional
    public Result updateOrderStatus(Integer orderId, String status, Integer operatorId) {
        try {
            // 1. 获取工单
            RepairOrder order = this.getById(orderId);
            if (order == null) {
                return Result.fail("工单不存在");
            }

            // 2. 验证操作者权限
            User operator = userMapper.selectById(operatorId);
            if (operator == null) {
                return Result.fail("操作者不存在");
            }

            // 3. 状态流转验证
            if (!isValidStatusTransition(order.getStatus(), status, operator.getRole())) {
                return Result.fail("状态流转不合法");
            }

            // 4. 特殊状态处理
            if (STATUS_QUOTE_PENDING.equals(status)) {
                // 待确认报价状态需要设置预估金额
                if (order.getEstimatedAmount() == null || order.getEstimatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    return Result.fail("请先设置预估金额");
                }
            }

            if (STATUS_COMPLETED.equals(status)) {
                // 完成状态需要设置实际金额
                if (order.getActualAmount() == null || order.getActualAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    return Result.fail("请先设置实际金额");
                }
            }

            // 5. 更新状态
            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());

            boolean updated = this.updateById(order);
            return updated ? Result.suc("状态更新成功", order) : Result.fail("状态更新失败");
        } catch (Exception e) {
            return Result.fail("更新状态失败: " + e.getMessage());
        }
    }

    // 补回isValidStatusTransition方法（updateOrderStatus依赖这个工具方法）
    private boolean isValidStatusTransition(String currentStatus, String newStatus, String operatorRole) {
        // 状态流转规则
        switch (currentStatus) {
            case STATUS_PENDING:
                // 待接车 → 检查中（只能由服务顾问操作）
                return STATUS_INSPECTING.equals(newStatus) && "service".equals(operatorRole);

            case STATUS_INSPECTING:
                // 检查中 → 待确认报价（只能由服务顾问操作）
                if (STATUS_QUOTE_PENDING.equals(newStatus)) {
                    return "service".equals(operatorRole);
                }
                return false;

            case STATUS_QUOTE_PENDING:
                // 待确认报价 → 维修中（车主确认后）
                if (STATUS_REPAIRING.equals(newStatus)) {
                    return "owner".equals(operatorRole) || "service".equals(operatorRole);
                }
                return false;

            case STATUS_REPAIRING:
                // 维修中 → 已完成（只能由维修技师操作）
                if (STATUS_COMPLETED.equals(newStatus)) {
                    return "mechanic".equals(operatorRole);
                }
                return false;

            case STATUS_COMPLETED:
                // 已完成 → 已交车（只能由服务顾问操作）
                if (STATUS_DELIVERED.equals(newStatus)) {
                    return "service".equals(operatorRole);
                }
                return false;

            case STATUS_DELIVERED:
                // 已交车状态不能再修改
                return false;

            default:
                return false;
        }
    }

    // ... 这里继续保留你原本的其他方法（assignMechanic、addOrderItem等）

    // 你添加的getOrdersByMechanicId方法（保持不变）
    @Override
    public Result getOrdersByMechanicId(Integer mechanicId, String status) {
        // ... 你的getOrdersByMechanicId实现代码（保持不变）
    }
}