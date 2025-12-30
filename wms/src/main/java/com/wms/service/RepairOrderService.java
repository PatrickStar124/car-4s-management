package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.common.Result;
import com.wms.entity.RepairOrder;

import java.math.BigDecimal;
import java.util.List;

public interface RepairOrderService extends IService<RepairOrder> {

    /**
     * 创建维修工单
     */
    Result createOrder(RepairOrder order);

    /**
     * 更新工单状态（核心状态流转逻辑）
     */
    Result updateOrderStatus(Integer orderId, String status, Integer operatorId);

    /**
     * 分配维修技师
     */
    Result assignMechanic(Integer orderId, Integer mechanicId);

    /**
     * 添加工单项（工时、配件等）
     */
    Result addOrderItem(Integer orderId, Integer partId, String itemType,
                        String description, Integer quantity, BigDecimal unitPrice);

    /**
     * 计算工单总金额
     */
    Result calculateOrderAmount(Integer orderId);

    /**
     * 根据车主ID查询工单（车主端API）
     */
    Result getOrdersByOwnerId(Integer ownerId);

    /**
     * 根据车辆ID查询维修历史
     */
    Result getOrdersByVehicleId(Integer vehicleId);

    /**
     * 根据状态查询工单（如：待接车、维修中等）
     */
    Result getOrdersByStatus(String status);

    /**
     * 获取工单详情（包含关联信息）
     */
    Result getOrderDetail(Integer orderId);

    /**
     * 提交报价给车主确认
     */
    Result submitQuote(Integer orderId, BigDecimal estimatedAmount, String remark);

    /**
     * 完成工单结算
     */
    Result completeOrder(Integer orderId, BigDecimal actualAmount);
}