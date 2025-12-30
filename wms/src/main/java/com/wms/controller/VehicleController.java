package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Vehicle;
import com.wms.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 车辆管理控制器
 * 处理车辆相关的接口请求，包括添加、更新、查询、删除及维修历史查询等操作
 */
@RestController
@RequestMapping("/api/vehicle")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    /**
     * 添加车辆信息
     * @param vehicle 车辆信息实体（包含车牌号、车主ID、车型等核心信息）
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result addVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.addVehicle(vehicle);
    }

    /**
     * 更新车辆信息
     * @param vehicle 待更新的车辆信息实体（需包含车辆ID）
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result updateVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.updateVehicle(vehicle);
    }

    /**
     * 根据车主ID查询其名下所有车辆
     * @param ownerId 车主ID
     * @return 该车主的车辆列表
     */
    @GetMapping("/owner/{ownerId}")
    public Result getVehiclesByOwner(@PathVariable Integer ownerId) {
        return vehicleService.getVehiclesByOwnerId(ownerId);
    }

    /**
     * 根据车牌号查询车辆信息
     * @param plateNumber 车牌号（精确匹配）
     * @return 对应的车辆详情
     */
    @GetMapping("/plate/{plateNumber}")
    public Result getVehicleByPlate(@PathVariable String plateNumber) {
        return vehicleService.getVehicleByPlate(plateNumber);
    }

    /**
     * 根据车辆ID获取车辆详情
     * @param id 车辆ID
     * @return 车辆的完整信息
     */
    @GetMapping("/detail/{id}")
    public Result getVehicleDetail(@PathVariable Integer id) {
        return vehicleService.getVehicleDetail(id);
    }

    /**
     * 查询车辆的维修历史记录
     * @param vehicleId 车辆ID
     * @return 该车辆的所有维修工单记录
     */
    @GetMapping("/history/{vehicleId}")
    public Result getVehicleRepairHistory(@PathVariable Integer vehicleId) {
        return vehicleService.getVehicleRepairHistory(vehicleId);
    }

    /**
     * 获取所有车辆列表
     * @return 全量车辆信息列表
     */
    @GetMapping("/list")
    public Result getVehicleList() {
        return Result.suc(vehicleService.list());
    }

    /**
     * 根据车辆ID删除车辆信息
     * @param id 车辆ID
     * @return 操作结果（成功/失败提示）
     */
    @DeleteMapping("/{id}")
    public Result deleteVehicle(@PathVariable Integer id) {
        boolean removed = vehicleService.removeById(id);
        return removed ? Result.suc("删除成功") : Result.fail("删除失败");
    }
}