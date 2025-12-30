package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.Result;
import com.wms.entity.Appointment;
import com.wms.entity.User;
import com.wms.entity.Vehicle;
import com.wms.mapper.AppointmentMapper;
import com.wms.mapper.UserMapper;
import com.wms.mapper.VehicleMapper;
import com.wms.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment>
        implements AppointmentService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    // 预约状态常量
    private static final String STATUS_PENDING = "pending";    // 待确认
    private static final String STATUS_CONFIRMED = "confirmed"; // 已确认
    private static final String STATUS_CANCELED = "canceled";  // 已取消
    private static final String STATUS_COMPLETED = "completed"; // 已完成

    @Override
    @Transactional
    public Result createAppointment(Appointment appointment) {
        try {
            // 1. 验证必填字段
            if (appointment.getVehicleId() == null) {
                return Result.fail("车辆不能为空");
            }
            if (appointment.getOwnerId() == null) {
                return Result.fail("车主不能为空");
            }
            if (appointment.getStartTime() == null || appointment.getEndTime() == null) {
                return Result.fail("预约时间不能为空");
            }
            if (appointment.getServiceType() == null || appointment.getServiceType().trim().isEmpty()) {
                return Result.fail("服务类型不能为空");
            }

            // 2. 验证时间有效性
            if (appointment.getStartTime().isAfter(appointment.getEndTime())) {
                return Result.fail("开始时间不能晚于结束时间");
            }

            // 3. 验证预约时长（最少30分钟，最多4小时）
            long durationMinutes = java.time.Duration.between(
                    appointment.getStartTime(), appointment.getEndTime()).toMinutes();
            if (durationMinutes < 30) {
                return Result.fail("预约时间最少30分钟");
            }
            if (durationMinutes > 240) {
                return Result.fail("预约时间最多4小时");
            }

            // 4. 验证车主和车辆
            Vehicle vehicle = vehicleMapper.selectById(appointment.getVehicleId());
            if (vehicle == null) {
                return Result.fail("车辆不存在");
            }

            User owner = userMapper.selectById(appointment.getOwnerId());
            if (owner == null || !"owner".equals(owner.getRole())) {
                return Result.fail("车主信息无效");
            }

            // 5. 检查时间冲突（任务书要求）
            Result conflictCheck = checkTimeConflict(
                    appointment.getVehicleId(),
                    appointment.getStartTime(),
                    appointment.getEndTime()
            );
            if (conflictCheck.getCode() == 500) { // 有冲突
                return conflictCheck;
            }

            // 6. 生成预约号
            String appointmentNo = "AP" + System.currentTimeMillis();
            appointment.setAppointmentNo(appointmentNo);
            appointment.setStatus(STATUS_PENDING); // 初始状态：待确认
            appointment.setCreateTime(LocalDateTime.now());

            // 7. 保存预约
            boolean saved = this.save(appointment);
            return saved ? Result.suc("预约创建成功", appointment) : Result.fail("预约创建失败");
        } catch (Exception e) {
            return Result.fail("创建预约失败: " + e.getMessage());
        }
    }

    @Override
    public Result checkTimeConflict(Integer vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 查询该时间段内同一车辆的预约
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Appointment::getVehicleId, vehicleId)
                    .ne(Appointment::getStatus, STATUS_CANCELED) // 排除已取消的预约
                    .and(w -> w.between(Appointment::getStartTime, startTime, endTime)
                            .or()
                            .between(Appointment::getEndTime, startTime, endTime)
                            .or(w2 -> w2.le(Appointment::getStartTime, startTime)
                                    .ge(Appointment::getEndTime, endTime)));

            List<Appointment> conflicts = this.list(wrapper);

            if (!conflicts.isEmpty()) {
                StringBuilder conflictInfo = new StringBuilder("时间冲突：");
                for (Appointment conflict : conflicts) {
                    conflictInfo.append(String.format("\n%s (%s - %s)",
                            conflict.getAppointmentNo(),
                            conflict.getStartTime(),
                            conflict.getEndTime()));
                }
                return Result.fail(conflictInfo.toString());
            }

            return Result.suc("时间可用");
        } catch (Exception e) {
            return Result.fail("检查时间冲突失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result cancelAppointment(Integer appointmentId, Integer userId) {
        try {
            Appointment appointment = this.getById(appointmentId);
            if (appointment == null) {
                return Result.fail("预约不存在");
            }

            // 验证用户权限（车主只能取消自己的预约）
            if (!appointment.getOwnerId().equals(userId)) {
                User user = userMapper.selectById(userId);
                if (user == null || !"service".equals(user.getRole())) {
                    return Result.fail("没有权限取消此预约");
                }
            }

            // 检查预约状态（只能取消待确认或已确认的预约）
            if (!STATUS_PENDING.equals(appointment.getStatus()) &&
                    !STATUS_CONFIRMED.equals(appointment.getStatus())) {
                return Result.fail("当前状态不能取消");
            }

            // 检查时间（提前2小时可取消）
            LocalDateTime now = LocalDateTime.now();
            if (appointment.getStartTime().minusHours(2).isBefore(now)) {
                return Result.fail("距离预约开始不足2小时，不能取消");
            }

            appointment.setStatus(STATUS_CANCELED);
            boolean updated = this.updateById(appointment);

            return updated ? Result.suc("预约取消成功") : Result.fail("取消失败");
        } catch (Exception e) {
            return Result.fail("取消预约失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result confirmAppointment(Integer appointmentId, Integer serviceAdvisorId) {
        try {
            Appointment appointment = this.getById(appointmentId);
            if (appointment == null) {
                return Result.fail("预约不存在");
            }

            // 验证服务顾问
            User advisor = userMapper.selectById(serviceAdvisorId);
            if (advisor == null || !"service".equals(advisor.getRole())) {
                return Result.fail("服务顾问信息无效");
            }

            // 只有待确认的预约才能确认
            if (!STATUS_PENDING.equals(appointment.getStatus())) {
                return Result.fail("当前状态不能确认");
            }

            appointment.setStatus(STATUS_CONFIRMED);
            appointment.setServiceAdvisorId(serviceAdvisorId);
            appointment.setUpdateTime(LocalDateTime.now());

            boolean updated = this.updateById(appointment);
            return updated ? Result.suc("预约确认成功", appointment) : Result.fail("确认失败");
        } catch (Exception e) {
            return Result.fail("确认预约失败: " + e.getMessage());
        }
    }

    @Override
    public Result getAppointmentsByOwnerId(Integer ownerId) {
        try {
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Appointment::getOwnerId, ownerId)
                    .orderByDesc(Appointment::getStartTime);

            List<Appointment> appointments = this.list(wrapper);

            // 关联车辆信息
            appointments.forEach(appointment -> {
                if (appointment.getVehicleId() != null) {
                    appointment.setVehicle(vehicleMapper.selectById(appointment.getVehicleId()));
                }
                if (appointment.getServiceAdvisorId() != null) {
                    appointment.setServiceAdvisor(userMapper.selectById(appointment.getServiceAdvisorId()));
                }
            });

            return Result.suc(appointments);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result getAppointmentsByAdvisorId(Integer advisorId) {
        try {
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Appointment::getServiceAdvisorId, advisorId)
                    .orderByDesc(Appointment::getStartTime);

            List<Appointment> appointments = this.list(wrapper);

            // 关联车辆和车主信息
            appointments.forEach(appointment -> {
                if (appointment.getVehicleId() != null) {
                    appointment.setVehicle(vehicleMapper.selectById(appointment.getVehicleId()));
                }
                if (appointment.getOwnerId() != null) {
                    appointment.setOwner(userMapper.selectById(appointment.getOwnerId()));
                }
            });

            return Result.suc(appointments);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result getAppointmentsByStatus(String status) {
        try {
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Appointment::getStatus, status)
                    .orderByAsc(Appointment::getStartTime);

            List<Appointment> appointments = this.list(wrapper);
            return Result.suc(appointments);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result getTodayAppointments() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Appointment::getStartTime, startOfDay, endOfDay)
                    .ne(Appointment::getStatus, STATUS_CANCELED)
                    .orderByAsc(Appointment::getStartTime);

            List<Appointment> appointments = this.list(wrapper);

            // 统计信息
            long pendingCount = appointments.stream()
                    .filter(a -> STATUS_PENDING.equals(a.getStatus())).count();
            long confirmedCount = appointments.stream()
                    .filter(a -> STATUS_CONFIRMED.equals(a.getStatus())).count();

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("appointments", appointments);
            result.put("total", appointments.size());
            result.put("pendingCount", pendingCount);
            result.put("confirmedCount", confirmedCount);

            return Result.suc(result);
        } catch (Exception e) {
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public List<Appointment> getAppointmentsInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Appointment::getStartTime, startTime, endTime)
                .or()
                .between(Appointment::getEndTime, startTime, endTime)
                .ne(Appointment::getStatus, STATUS_CANCELED);

        return this.list(wrapper);
    }
}