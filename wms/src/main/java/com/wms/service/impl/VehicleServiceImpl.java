package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.Result;
import com.wms.entity.User;
import com.wms.entity.Vehicle;
import com.wms.mapper.UserMapper;
import com.wms.mapper.VehicleMapper;
import com.wms.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    @Autowired
    private UserMapper userMapper;

    // 根据车主ID获取其名下所有车辆
    @Override
    public Result getVehiclesByOwnerId(Integer ownerId) {
        try {
            LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Vehicle::getOwnerId, ownerId);
            List<Vehicle> vehicles = this.list(wrapper);

            // 关联车主信息
            vehicles.forEach(vehicle -> {
                if (vehicle.getOwnerId() != null) {
                    User owner = userMapper.selectById(vehicle.getOwnerId());
                    vehicle.setOwner(owner);
                }
            });

            return Result.suc(vehicles);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    // 根据车牌号查询车辆
    @Override
    public Result getVehicleByPlate(String plateNumber) {
        try {
            LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Vehicle::getPlateNumber, plateNumber);
            Vehicle vehicle = this.getOne(wrapper);

            if (vehicle == null) {
                return Result.fail("车辆不存在");
            }

            // 关联车主信息
            if (vehicle.getOwnerId() != null) {
                User owner = userMapper.selectById(vehicle.getOwnerId());
                vehicle.setOwner(owner);
            }

            return Result.suc(vehicle);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    // 添加新车辆
    @Override
    @Transactional
    public Result addVehicle(Vehicle vehicle) {
        try {
            // 1. 验证必填字段
            if (vehicle.getPlateNumber() == null || vehicle.getPlateNumber().trim().isEmpty()) {
                return Result.fail("车牌号不能为空");
            }
            if (vehicle.getOwnerId() == null) {
                return Result.fail("车主不能为空");
            }

            // 2. 验证车主是否存在
            User owner = userMapper.selectById(vehicle.getOwnerId());
            if (owner == null || !"owner".equals(owner.getRole())) {
                return Result.fail("车主信息无效");
            }

            // 3. 验证车牌号是否重复
            LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Vehicle::getPlateNumber, vehicle.getPlateNumber());
            if (this.count(wrapper) > 0) {
                return Result.fail("车牌号已存在");
            }

            // 4. 保存车辆
            boolean saved = this.save(vehicle);
            return saved ? Result.suc("车辆添加成功", vehicle) : Result.fail("添加失败");
        } catch (Exception e) {
            return Result.fail("添加失败: " + e.getMessage());
        }
    }

    // 更新车辆信息
    @Override
    @Transactional
    public Result updateVehicle(Vehicle vehicle) {
        try {
            if (vehicle.getId() == null) {
                return Result.fail("车辆ID不能为空");
            }

            // 检查车辆是否存在
            Vehicle existing = this.getById(vehicle.getId());
            if (existing == null) {
                return Result.fail("车辆不存在");
            }

            // 如果更新了车牌号，检查是否重复
            if (vehicle.getPlateNumber() != null &&
                    !vehicle.getPlateNumber().equals(existing.getPlateNumber())) {
                LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Vehicle::getPlateNumber, vehicle.getPlateNumber());
                if (this.count(wrapper) > 0) {
                    return Result.fail("车牌号已存在");
                }
            }

            boolean updated = this.updateById(vehicle);
            return updated ? Result.suc("车辆更新成功", vehicle) : Result.fail("更新失败");
        } catch (Exception e) {
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    // 获取车辆详细信息（包含车主信息）
    @Override
    public Result getVehicleDetail(Integer vehicleId) {
        try {
            Vehicle vehicle = this.getById(vehicleId);
            if (vehicle == null) {
                return Result.fail("车辆不存在");
            }

            // 关联车主信息
            if (vehicle.getOwnerId() != null) {
                User owner = userMapper.selectById(vehicle.getOwnerId());
                vehicle.setOwner(owner);
            }

            return Result.suc(vehicle);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    // 获取车辆维修历史（TODO：待关联维修工单模块）
    @Override
    public Result getVehicleRepairHistory(Integer vehicleId) {
        try {
            // TODO: 这里需要关联RepairOrder表查询维修历史
            // 暂时返回提示信息
            return Result.suc("维修历史查询功能将在工单模块完成后实现");
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }
}