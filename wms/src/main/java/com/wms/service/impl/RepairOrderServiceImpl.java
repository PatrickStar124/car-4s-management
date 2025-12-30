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

    @Override
    @Transactional
    public Result createOrder(RepairOrder order) {
        try {
            // 1. 验证必填字段
            if (order.getVehicleId() == null) {
                return Result.fail("车辆不能为空");
            }
            if (order.getOwnerId() == null) {
                return Result.fail("车主不能为空");
            }
            if (order.getServiceAdvisorId() == null) {
                return Result.fail("服务顾问不能为空");
            }

            // 2. 验证车辆和车主
            Vehicle vehicle = vehicleMapper.selectById(order.getVehicleId());
            if (vehicle == null) {
                return Result.fail("车辆不存在");
            }

            User owner = userMapper.selectById(order.getOwnerId());
            if (owner == null || !"owner".equals(owner.getRole())) {
                return Result.fail("车主信息无效");
            }

            // 3. 验证服务顾问
            User serviceAdvisor = userMapper.selectById(order.getServiceAdvisorId());
            if (serviceAdvisor == null || !"service".equals(serviceAdvisor.getRole())) {
                return Result.fail("服务顾问信息无效");
            }

            // 4. 生成工单号
            String orderNo = "RO" + System.currentTimeMillis();
            order.setOrderNo(orderNo);
            order.setStatus(STATUS_PENDING); // 初始状态：待接车
            order.setCreateTime(LocalDateTime.now());

            // 5. 保存工单
            boolean saved = this.save(order);
            return saved ? Result.suc("工单创建成功", order) : Result.fail("工单创建失败");
        } catch (Exception e) {
            return Result.fail("创建工单失败: " + e.getMessage());
        }
    }

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

    /**
     * 验证状态流转是否合法
     */
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

    @Override
    @Transactional
    public Result assignMechanic(Integer orderId, Integer mechanicId) {
        try {
            RepairOrder order = this.getById(orderId);
            if (order == null) {
                return Result.fail("工单不存在");
            }

            // 验证工单状态（只有在维修中或待接车时才能分配技师）
            if (!STATUS_REPAIRING.equals(order.getStatus()) && !STATUS_PENDING.equals(order.getStatus())) {
                return Result.fail("当前工单状态不能分配技师");
            }

            // 验证维修技师
            User mechanic = userMapper.selectById(mechanicId);
            if (mechanic == null || !"mechanic".equals(mechanic.getRole())) {
                return Result.fail("维修技师信息无效");
            }

            // 更新维修技师
            order.setMechanicId(mechanicId);
            order.setUpdateTime(LocalDateTime.now());

            boolean updated = this.updateById(order);
            return updated ? Result.suc("技师分配成功", order) : Result.fail("技师分配失败");
        } catch (Exception e) {
            return Result.fail("分配技师失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result addOrderItem(Integer orderId, Integer partId, String itemType,
                               String description, Integer quantity, BigDecimal unitPrice) {
        try {
            // 1. 验证工单
            RepairOrder order = this.getById(orderId);
            if (order == null) {
                return Result.fail("工单不存在");
            }

            // 2. 工单必须在维修中或检查中才能添加工单项
            if (!STATUS_REPAIRING.equals(order.getStatus()) && !STATUS_INSPECTING.equals(order.getStatus())) {
                return Result.fail("当前工单状态不能添加工单项");
            }

            // 3. 如果是配件类型，验证库存
            if ("part".equals(itemType) && partId != null) {
                Part part = partMapper.selectById(partId);
                if (part == null) {
                    return Result.fail("配件不存在");
                }

                Inventory inventory = inventoryMapper.selectById(partId);
                if (inventory == null || inventory.getStockQuantity() < quantity) {
                    return Result.fail("配件库存不足");
                }

                // 锁定库存
                inventory.setLockedQuantity(inventory.getLockedQuantity() + quantity);
                inventoryMapper.updateById(inventory);
            }

            // 4. 创建工单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setPartId(partId);
            orderItem.setItemType(itemType);
            orderItem.setDescription(description);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);

            // 计算总价
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
            orderItem.setTotalPrice(totalPrice);

            // 5. 保存工单项
            boolean saved = orderItemMapper.insert(orderItem) > 0;
            return saved ? Result.suc("工单项添加成功", orderItem) : Result.fail("添加工单项失败");
        } catch (Exception e) {
            return Result.fail("添加工单项失败: " + e.getMessage());
        }
    }

    @Override
    public Result calculateOrderAmount(Integer orderId) {
        try {
            // 查询该工单的所有工单项
            LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(wrapper);

            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItem item : items) {
                if (item.getTotalPrice() != null) {
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }

            // 更新工单金额
            RepairOrder order = this.getById(orderId);
            if (order != null) {
                order.setEstimatedAmount(totalAmount);
                this.updateById(order);
            }

            return Result.suc(totalAmount);
        } catch (Exception e) {
            return Result.fail("计算金额失败: " + e.getMessage());
        }
    }

    @Override
    public Result getOrdersByOwnerId(Integer ownerId) {
        try {
            LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RepairOrder::getOwnerId, ownerId)
                    .orderByDesc(RepairOrder::getCreateTime);

            List<RepairOrder> orders = this.list(wrapper);

            // 关联车辆和服务顾问信息
            orders.forEach(order -> {
                if (order.getVehicleId() != null) {
                    order.setVehicle(vehicleMapper.selectById(order.getVehicleId()));
                }
                if (order.getServiceAdvisorId() != null) {
                    order.setServiceAdvisor(userMapper.selectById(order.getServiceAdvisorId()));
                }
            });

            return Result.suc(orders);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result getOrdersByVehicleId(Integer vehicleId) {
        try {
            LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RepairOrder::getVehicleId, vehicleId)
                    .orderByDesc(RepairOrder::getCreateTime);

            List<RepairOrder> orders = this.list(wrapper);
            return Result.suc(orders);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result getOrdersByStatus(String status) {
        try {
            LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RepairOrder::getStatus, status)
                    .orderByDesc(RepairOrder::getCreateTime);

            List<RepairOrder> orders = this.list(wrapper);
            return Result.suc(orders);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result getOrderDetail(Integer orderId) {
        try {
            RepairOrder order = this.getById(orderId);
            if (order == null) {
                return Result.fail("工单不存在");
            }

            // 关联车辆信息
            if (order.getVehicleId() != null) {
                order.setVehicle(vehicleMapper.selectById(order.getVehicleId()));
            }

            // 关联车主信息
            if (order.getOwnerId() != null) {
                order.setOwner(userMapper.selectById(order.getOwnerId()));
            }

            // 关联服务顾问信息
            if (order.getServiceAdvisorId() != null) {
                order.setServiceAdvisor(userMapper.selectById(order.getServiceAdvisorId()));
            }

            // 关联维修技师信息
            if (order.getMechanicId() != null) {
                order.setMechanic(userMapper.selectById(order.getMechanicId()));
            }

            // 查询工单项
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

            // 关联配件信息（已修正！）
            items.forEach(item -> {
                if (item.getPartId() != null) {
                    // 直接设置，不需要强制类型转换
                    item.setPart(partMapper.selectById(item.getPartId()));
                }
            });

            // 返回完整信息
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("order", order);
            result.put("items", items);

            return Result.suc(result);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result submitQuote(Integer orderId, BigDecimal estimatedAmount, String remark) {
        try {
            RepairOrder order = this.getById(orderId);
            if (order == null) {
                return Result.fail("工单不存在");
            }

            // 只有在检查中或待确认报价状态才能提交报价
            if (!STATUS_INSPECTING.equals(order.getStatus()) &&
                    !STATUS_QUOTE_PENDING.equals(order.getStatus())) {
                return Result.fail("当前工单状态不能提交报价");
            }

            // 更新报价信息
            order.setEstimatedAmount(estimatedAmount);
            order.setStatus(STATUS_QUOTE_PENDING); // 更新为待确认报价状态
            order.setRemark(remark);
            order.setUpdateTime(LocalDateTime.now());

            boolean updated = this.updateById(order);
            return updated ? Result.suc("报价提交成功", order) : Result.fail("报价提交失败");
        } catch (Exception e) {
            return Result.fail("提交报价失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result completeOrder(Integer orderId, BigDecimal actualAmount) {
        try {
            RepairOrder order = this.getById(orderId);
            if (order == null) {
                return Result.fail("工单不存在");
            }

            // 只有在维修中状态才能完成
            if (!STATUS_REPAIRING.equals(order.getStatus())) {
                return Result.fail("当前工单状态不能完成");
            }

            // 更新工单信息
            order.setActualAmount(actualAmount);
            order.setStatus(STATUS_COMPLETED);
            order.setUpdateTime(LocalDateTime.now());

            // 如果是配件，扣减库存
            if (order.getId() != null) {
                LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(OrderItem::getOrderId, orderId)
                        .eq(OrderItem::getItemType, "part");

                List<OrderItem> partItems = orderItemMapper.selectList(wrapper);
                for (OrderItem item : partItems) {
                    if (item.getPartId() != null) {
                        Inventory inventory = inventoryMapper.selectById(item.getPartId());
                        if (inventory != null) {
                            // 扣减库存并释放锁定数量
                            int newStock = inventory.getStockQuantity() - item.getQuantity();
                            int newLocked = inventory.getLockedQuantity() - item.getQuantity();

                            inventory.setStockQuantity(Math.max(newStock, 0));
                            inventory.setLockedQuantity(Math.max(newLocked, 0));
                            inventory.setLastOutboundTime(LocalDateTime.now());

                            inventoryMapper.updateById(inventory);
                        }
                    }
                }
            }

            boolean updated = this.updateById(order);
            return updated ? Result.suc("工单完成", order) : Result.fail("工单完成失败");
        } catch (Exception e) {
            return Result.fail("完成工单失败: " + e.getMessage());
        }
    }
}