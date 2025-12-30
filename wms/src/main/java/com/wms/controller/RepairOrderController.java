package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.RepairOrder;
import com.wms.service.RepairOrderService;
// 删除下面两行
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/repair-order")
// 删除这一行：@Api(tags = "维修工单管理")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @PostMapping("/create")
    // 删除这一行：@ApiOperation("创建维修工单")
    public Result createOrder(@RequestBody RepairOrder order) {
        return repairOrderService.createOrder(order);
    }

    @PutMapping("/status/{orderId}")
    // 删除这一行：@ApiOperation("更新工单状态")
    public Result updateOrderStatus(@PathVariable Integer orderId,
                                    @RequestParam String status,
                                    @RequestParam Integer operatorId) {
        return repairOrderService.updateOrderStatus(orderId, status, operatorId);
    }

    @PutMapping("/assign-mechanic/{orderId}")
    // 删除这一行：@ApiOperation("分配维修技师")
    public Result assignMechanic(@PathVariable Integer orderId,
                                 @RequestParam Integer mechanicId) {
        return repairOrderService.assignMechanic(orderId, mechanicId);
    }

    @PostMapping("/add-item/{orderId}")
    // 删除这一行：@ApiOperation("添加工单项")
    public Result addOrderItem(@PathVariable Integer orderId,
                               @RequestParam(required = false) Integer partId,
                               @RequestParam String itemType,
                               @RequestParam String description,
                               @RequestParam Integer quantity,
                               @RequestParam BigDecimal unitPrice) {
        return repairOrderService.addOrderItem(orderId, partId, itemType, description, quantity, unitPrice);
    }

    @GetMapping("/calculate/{orderId}")
    // 删除这一行：@ApiOperation("计算工单金额")
    public Result calculateOrderAmount(@PathVariable Integer orderId) {
        return repairOrderService.calculateOrderAmount(orderId);
    }

    @GetMapping("/owner/{ownerId}")
    // 删除这一行：@ApiOperation("车主查询工单")
    public Result getOrdersByOwner(@PathVariable Integer ownerId) {
        return repairOrderService.getOrdersByOwnerId(ownerId);
    }

    @GetMapping("/vehicle/{vehicleId}")
    // 删除这一行：@ApiOperation("查询车辆工单")
    public Result getOrdersByVehicle(@PathVariable Integer vehicleId) {
        return repairOrderService.getOrdersByVehicleId(vehicleId);
    }

    @GetMapping("/status/{status}")
    // 删除这一行：@ApiOperation("根据状态查询工单")
    public Result getOrdersByStatus(@PathVariable String status) {
        return repairOrderService.getOrdersByStatus(status);
    }

    @GetMapping("/detail/{orderId}")
    // 删除这一行：@ApiOperation("获取工单详情")
    public Result getOrderDetail(@PathVariable Integer orderId) {
        return repairOrderService.getOrderDetail(orderId);
    }

    @PostMapping("/submit-quote/{orderId}")
    // 删除这一行：@ApiOperation("提交报价")
    public Result submitQuote(@PathVariable Integer orderId,
                              @RequestParam BigDecimal estimatedAmount,
                              @RequestParam(required = false) String remark) {
        return repairOrderService.submitQuote(orderId, estimatedAmount, remark);
    }

    @PostMapping("/complete/{orderId}")
    // 删除这一行：@ApiOperation("完成工单")
    public Result completeOrder(@PathVariable Integer orderId,
                                @RequestParam BigDecimal actualAmount) {
        return repairOrderService.completeOrder(orderId, actualAmount);
    }

    @GetMapping("/list")
    // 删除这一行：@ApiOperation("获取工单列表")
    public Result getOrderList() {
        return Result.suc(repairOrderService.list());
    }
}