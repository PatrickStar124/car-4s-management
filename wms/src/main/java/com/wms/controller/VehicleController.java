package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Vehicle;
import com.wms.service.VehicleService;
// 删除下面两行
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicle")
// 删除这一行：@Api(tags = "车辆管理")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/add")
    // 删除这一行：@ApiOperation("添加车辆")
    public Result addVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.addVehicle(vehicle);
    }

    @PutMapping("/update")
    // 删除这一行：@ApiOperation("更新车辆信息")
    public Result updateVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.updateVehicle(vehicle);
    }

    @GetMapping("/owner/{ownerId}")
    // 删除这一行：@ApiOperation("根据车主查询车辆")
    public Result getVehiclesByOwner(@PathVariable Integer ownerId) {
        return vehicleService.getVehiclesByOwnerId(ownerId);
    }

    @GetMapping("/plate/{plateNumber}")
    // 删除这一行：@ApiOperation("根据车牌号查询车辆")
    public Result getVehicleByPlate(@PathVariable String plateNumber) {
        return vehicleService.getVehicleByPlate(plateNumber);
    }

    @GetMapping("/detail/{id}")
    // 删除这一行：@ApiOperation("获取车辆详情")
    public Result getVehicleDetail(@PathVariable Integer id) {
        return vehicleService.getVehicleDetail(id);
    }

    @GetMapping("/history/{vehicleId}")
    // 删除这一行：@ApiOperation("获取车辆维修历史")
    public Result getVehicleRepairHistory(@PathVariable Integer vehicleId) {
        return vehicleService.getVehicleRepairHistory(vehicleId);
    }

    @GetMapping("/list")
    // 删除这一行：@ApiOperation("获取车辆列表")
    public Result getVehicleList() {
        return Result.suc(vehicleService.list());
    }

    @DeleteMapping("/{id}")
    // 删除这一行：@ApiOperation("删除车辆")
    public Result deleteVehicle(@PathVariable Integer id) {
        boolean removed = vehicleService.removeById(id);
        return removed ? Result.suc("删除成功") : Result.fail("删除失败");
    }
}