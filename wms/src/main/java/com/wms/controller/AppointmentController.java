package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Appointment;
import com.wms.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 预约管理控制器
 * 处理预约相关的接口请求，包括创建、冲突检查、取消、确认、查询等操作
 * 接口排序逻辑：基础查询 → 核心操作 → 特殊场景查询
 */
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // ======================== 基础查询接口（通用/全量查询）========================
    /**
     * 获取所有预约列表（全量查询）
     * 用途：后台管理端查看所有预约数据，支持分页/筛选扩展
     * @return 全量预约信息（Result封装）
     */
    @GetMapping("/list")
    public Result getAppointmentList() {
        return Result.suc(appointmentService.list());
    }

    /**
     * 根据预约状态查询预约列表
     * 用途：按状态筛选预约（如：待确认、已确认、已取消、已完成）
     * @param status 预约状态字符串
     * @return 对应状态的所有预约信息
     */
    @GetMapping("/status/{status}")
    public Result getAppointmentsByStatus(@PathVariable String status) {
        return appointmentService.getAppointmentsByStatus(status);
    }

    /**
     * 获取今日所有预约
     * 用途：每日工作台展示当日待处理的预约
     * @return 今日的预约列表
     */
    @GetMapping("/today")
    public Result getTodayAppointments() {
        return appointmentService.getTodayAppointments();
    }

    // ======================== 关联主体查询接口（按车主/服务顾问）========================
    /**
     * 根据车主ID查询预约列表
     * 用途：车主端查看自己的所有预约记录
     * @param ownerId 车主ID
     * @return 该车主的所有预约信息
     */
    @GetMapping("/owner/{ownerId}")
    public Result getAppointmentsByOwner(@PathVariable Integer ownerId) {
        return appointmentService.getAppointmentsByOwnerId(ownerId);
    }

    /**
     * 根据服务顾问ID查询预约列表
     * 用途：服务顾问端查看自己负责的预约
     * @param advisorId 服务顾问ID
     * @return 该服务顾问负责的所有预约信息
     */
    @GetMapping("/advisor/{advisorId}")
    public Result getAppointmentsByAdvisor(@PathVariable Integer advisorId) {
        return appointmentService.getAppointmentsByAdvisorId(advisorId);
    }

    // ======================== 核心操作接口（创建/检查/修改）========================
    /**
     * 检查车辆预约时间冲突
     * 用途：创建预约前的前置校验，避免同一车辆同一时间段重复预约
     * @param vehicleId 车辆ID
     * @param startTime 预约开始时间（字符串格式，需符合LocalDateTime解析规则）
     * @param endTime 预约结束时间（字符串格式，需符合LocalDateTime解析规则）
     * @return 冲突检查结果（存在冲突返回失败，无冲突返回成功）
     */
    @PostMapping("/check-conflict")
    public Result checkTimeConflict(@RequestParam Integer vehicleId,
                                    @RequestParam String startTime,
                                    @RequestParam String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        return appointmentService.checkTimeConflict(vehicleId, start, end);
    }

    /**
     * 创建预约
     * 用途：车主/管理员创建新的预约（建议先调用冲突检查接口）
     * @param appointment 预约信息实体（包含车辆、时间、车主等信息）
     * @return 操作结果（成功/失败+提示信息）
     */
    @PostMapping("/create")
    public Result createAppointment(@RequestBody Appointment appointment) {
        return appointmentService.createAppointment(appointment);
    }

    /**
     * 确认预约
     * 用途：服务顾问确认预约（变更预约状态为"已确认"）
     * @param appointmentId 预约ID
     * @param serviceAdvisorId 服务顾问ID（操作人标识）
     * @return 操作结果
     */
    @PutMapping("/confirm/{appointmentId}")
    public Result confirmAppointment(@PathVariable Integer appointmentId,
                                     @RequestParam Integer serviceAdvisorId) {
        return appointmentService.confirmAppointment(appointmentId, serviceAdvisorId);
    }

    /**
     * 取消预约
     * 用途：车主/管理员取消已创建的预约（变更预约状态为"已取消"）
     * @param appointmentId 预约ID
     * @param userId 操作人ID（车主ID，用于校验操作权限）
     * @return 操作结果
     */
    @PutMapping("/cancel/{appointmentId}")
    public Result cancelAppointment(@PathVariable Integer appointmentId,
                                    @RequestParam Integer userId) {
        return appointmentService.cancelAppointment(appointmentId, userId);
    }
}