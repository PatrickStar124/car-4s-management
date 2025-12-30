package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.common.Result;
import com.wms.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService extends IService<Appointment> {

    /**
     * 创建预约
     */
    Result createAppointment(Appointment appointment);

    /**
     * 取消预约
     */
    Result cancelAppointment(Integer appointmentId, Integer userId);

    /**
     * 确认预约（服务顾问操作）
     */
    Result confirmAppointment(Integer appointmentId, Integer serviceAdvisorId);

    /**
     * 检查预约时间冲突
     */
    Result checkTimeConflict(Integer vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询用户的预约记录
     */
    Result getAppointmentsByOwnerId(Integer ownerId);

    /**
     * 查询服务顾问的预约
     */
    Result getAppointmentsByAdvisorId(Integer advisorId);

    /**
     * 根据状态查询预约
     */
    Result getAppointmentsByStatus(String status);

    /**
     * 获取今日预约
     */
    Result getTodayAppointments();

    /**
     * 获取某个时间段的预约（用于冲突检测）
     */
    List<Appointment> getAppointmentsInTimeRange(LocalDateTime startTime, LocalDateTime endTime);
}