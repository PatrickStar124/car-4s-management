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
 */
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 创建预约
     * @param appointment 预约信息实体
     * @return 操作结果
     */
    @PostMapping("/create")
    public Result createAppointment(@RequestBody Appointment appointment) {
        return appointmentService.createAppointment(appointment);
    }

    /**
     * 检查车辆预约时间冲突
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
     * 取消预约
     * @param appointmentId 预约ID
     * @param userId 操作人ID（车主ID）
     * @return 操作结果
     */
    @PutMapping("/cancel/{appointmentId}")
    public Result cancelAppointment(@PathVariable Integer appointmentId,
                                    @RequestParam Integer userId) {
        return appointmentService.cancelAppointment(appointmentId, userId);
    }

    /**
     * 确认预约
     * @param appointmentId 预约ID
     * @param serviceAdvisorId 服务顾问ID
     * @return 操作结果
     */
    @PutMapping("/confirm/{appointmentId}")
    public Result confirmAppointment(@PathVariable Integer appointmentId,
                                     @RequestParam Integer serviceAdvisorId) {
        return appointmentService.confirmAppointment(appointmentId, serviceAdvisorId);
    }

    /**
     * 根据车主ID查询预约列表
     * @param ownerId 车主ID
     * @return 该车主的所有预约信息
     */
    @GetMapping("/owner/{ownerId}")
    public Result getAppointmentsByOwner(@PathVariable Integer ownerId) {
        return appointmentService.getAppointmentsByOwnerId(ownerId);
    }

    /**
     * 根据服务顾问ID查询预约列表
     * @param advisorId 服务顾问ID
     * @return 该服务顾问负责的所有预约信息
     */
    @GetMapping("/advisor/{advisorId}")
    public Result getAppointmentsByAdvisor(@PathVariable Integer advisorId) {
        return appointmentService.getAppointmentsByAdvisorId(advisorId);
    }

    /**
     * 根据预约状态查询预约列表
     * @param status 预约状态（如：待确认、已确认、已取消、已完成等）
     * @return 对应状态的所有预约信息
     */
    @GetMapping("/status/{status}")
    public Result getAppointmentsByStatus(@PathVariable String status) {
        return appointmentService.getAppointmentsByStatus(status);
    }

    /**
     * 获取今日所有预约
     * @return 今日的预约列表
     */
    @GetMapping("/today")
    public Result getTodayAppointments() {
        return appointmentService.getTodayAppointments();
    }

    /**
     * 获取所有预约列表
     * @return 全量预约信息
     */
    @GetMapping("/list")
    public Result getAppointmentList() {
        return Result.suc(appointmentService.list());
    }
}