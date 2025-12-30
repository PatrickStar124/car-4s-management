package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.common.Result;
import com.wms.entity.Vehicle;

import java.util.List;

public interface VehicleService extends IService<Vehicle> {

    /**
     * 根据车主ID查询车辆
     */
    Result getVehiclesByOwnerId(Integer ownerId);

    /**
     * 根据车牌号查询车辆  ← 添加这个方法
     */
    Result getVehicleByPlate(String plateNumber);

    /**
     * 添加车辆
     */
    Result addVehicle(Vehicle vehicle);

    /**
     * 更新车辆信息
     */
    Result updateVehicle(Vehicle vehicle);

    /**
     * 根据车辆ID获取完整信息（包括车主信息）
     */
    Result getVehicleDetail(Integer vehicleId);

    /**
     * 获取车辆维修历史
     */
    Result getVehicleRepairHistory(Integer vehicleId);
}