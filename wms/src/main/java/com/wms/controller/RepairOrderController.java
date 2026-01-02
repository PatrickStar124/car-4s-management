package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Appointment;
import com.wms.entity.RepairOrder;
import com.wms.service.RepairOrderService;
import com.wms.service.AppointmentService; // ✅ 确保引入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 维修工单管理控制器
 * 处理维修工单相关的接口请求，包括创建、状态更新、技师分配、工单项管理、金额计算等操作
 */
@RestController
@RequestMapping("/api/repair-order")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * ✅ 新增接口：从预约单创建维修工单
     * @param appointmentId 预约单ID
     * @return 操作结果
     */
    @PostMapping("/from-appointment/{appointmentId}")
    public Result createFromAppointment(@PathVariable Integer appointmentId) {
        // 1. 获取预约单信息
        // 假设你的AppointmentService有getById方法
        Appointment appointment = appointmentService.getById(appointmentId);
        if (appointment == null) {
            return Result.fail("预约单不存在");
        }

        // 2. 检查预约单状态是否为已确认
        if (!"confirmed".equals(appointment.getStatus())) {
            return Result.fail("只有已确认的预约才能转为工单");
        }

        // 3. 将预约信息转换为工单信息
        RepairOrder order = new RepairOrder();
        order.setOrderNo("RO" + System.currentTimeMillis()); // 生成工单号
        order.setVehicleId(appointment.getVehicleId());
        order.setOwnerId(appointment.getOwnerId());
        order.setServiceAdvisorId(appointment.getServiceAdvisorId());
        order.setStatus("pending"); // 工单初始状态为“待接车”
        order.setProblemDesc("预约服务：" + appointment.getServiceType()); // 从预约服务类型生成问题描述
        order.setCreateTime(LocalDateTime.now()); // 使用当前时间创建工单

        // 4. 调用Service层创建工单
        return repairOrderService.createOrder(order);
    }
    /**
     * 创建维修工单
     * @param order 维修工单信息实体
     * @return 操作结果
     */
    @PostMapping("/create")
    public Result createOrder(@RequestBody RepairOrder order) {
        return repairOrderService.createOrder(order);
    }

    /**
     * 更新工单状态
     * @param orderId 工单ID
     * @param status 工单新状态（如：待报价、已报价、维修中、已完成、已取消等）
     * @param operatorId 操作人ID
     * @return 操作结果
     */
    @PutMapping("/status/{orderId}")
    public Result updateOrderStatus(@PathVariable Integer orderId,
                                    @RequestParam String status,
                                    @RequestParam Integer operatorId) {
        return repairOrderService.updateOrderStatus(orderId, status, operatorId);
    }

    /**
     * 为工单分配维修技师
     * @param orderId 工单ID
     * @param mechanicId 维修技师ID
     * @return 操作结果
     */
    @PutMapping("/assign-mechanic/{orderId}")
    public Result assignMechanic(@PathVariable Integer orderId,
                                 @RequestParam Integer mechanicId) {
        return repairOrderService.assignMechanic(orderId, mechanicId);
    }

    /**
     * 为工单添加工单项（配件/工时）
     * @param orderId 工单ID
     * @param partId 配件ID（非必传，工时类工单项可传null）
     * @param itemType 工单项类型（如：配件、工时）
     * @param description 工单项描述
     * @param quantity 数量
     * @param unitPrice 单价
     * @return 操作结果
     */
    @PostMapping("/add-item/{orderId}")
    public Result addOrderItem(@PathVariable Integer orderId,
                               @RequestParam(required = false) Integer partId,
                               @RequestParam String itemType,
                               @RequestParam String description,
                               @RequestParam Integer quantity,
                               @RequestParam BigDecimal unitPrice) {
        return repairOrderService.addOrderItem(orderId, partId, itemType, description, quantity, unitPrice);
    }

    /**
     * 计算工单总金额（基于所有工单项汇总）
     * @param orderId 工单ID
     * @return 包含工单总金额的操作结果
     */
    @GetMapping("/calculate/{orderId}")
    public Result calculateOrderAmount(@PathVariable Integer orderId) {
        return repairOrderService.calculateOrderAmount(orderId);
    }

    /**
     * 根据车主ID查询其所有维修工单
     * @param ownerId 车主ID
     * @return 该车主的维修工单列表
     */
    @GetMapping("/owner/{ownerId}")
    public Result getOrdersByOwner(@PathVariable Integer ownerId) {
        return repairOrderService.getOrdersByOwnerId(ownerId);
    }

    /**
     * 根据车辆ID查询其所有维修工单
     * @param vehicleId 车辆ID
     * @return 该车辆的维修工单列表
     */
    @GetMapping("/vehicle/{vehicleId}")
    public Result getOrdersByVehicle(@PathVariable Integer vehicleId) {
        return repairOrderService.getOrdersByVehicleId(vehicleId);
    }

    /**
     * 根据工单状态查询工单列表
     * @param status 工单状态（如：待报价、已报价、维修中、已完成、已取消等）
     * @return 对应状态的工单列表
     */
    @GetMapping("/status/{status}")
    public Result getOrdersByStatus(@PathVariable String status) {
        return repairOrderService.getOrdersByStatus(status);
    }

    /**
     * 获取工单详情（包含工单项、技师、金额等完整信息）
     * @param orderId 工单ID
     * @return 工单详情信息
     */
    @GetMapping("/detail/{orderId}")
    public Result getOrderDetail(@PathVariable Integer orderId) {
        return repairOrderService.getOrderDetail(orderId);
    }

    /**
     * 提交工单报价
     * @param orderId 工单ID
     * @param estimatedAmount 预估金额
     * @param remark 报价备注（非必传）
     * @return 操作结果
     */
    @PostMapping("/submit-quote/{orderId}")
    public Result submitQuote(@PathVariable Integer orderId,
                              @RequestParam BigDecimal estimatedAmount,
                              @RequestParam(required = false) String remark) {
        return repairOrderService.submitQuote(orderId, estimatedAmount, remark);
    }

    /**
     * 完成维修工单
     * @param orderId 工单ID
     * @param actualAmount 实际产生的金额
     * @return 操作结果
     */
    @PostMapping("/complete/{orderId}")
    public Result completeOrder(@PathVariable Integer orderId,
                                @RequestParam BigDecimal actualAmount) {
        return repairOrderService.completeOrder(orderId, actualAmount);
    }

    /**
     * 获取所有维修工单列表
     * @return 全量工单信息
     */
    @GetMapping("/list")
    public Result getOrderList() {
        return Result.suc(repairOrderService.list());
    }
}