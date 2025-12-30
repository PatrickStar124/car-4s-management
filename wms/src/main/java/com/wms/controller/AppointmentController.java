package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Appointment;
import com.wms.service.AppointmentService;
// 删除下面两行
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointment")
// 删除这一行：@Api(tags = "预约管理")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/create")
    // 删除这一行：@ApiOperation("创建预约")
    public Result createAppointment(@RequestBody Appointment appointment) {
        return appointmentService.createAppointment(appointment);
    }

    @PostMapping("/check-conflict")
    // 删除这一行：@ApiOperation("检查时间冲突")
    public Result checkTimeConflict(@RequestParam Integer vehicleId,
                                    @RequestParam String startTime,
                                    @RequestParam String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        return appointmentService.checkTimeConflict(vehicleId, start, end);
    }

    @PutMapping("/cancel/{appointmentId}")
    // 删除这一行：@ApiOperation("取消预约")
    public Result cancelAppointment(@PathVariable Integer appointmentId,
                                    @RequestParam Integer userId) {
        return appointmentService.cancelAppointment(appointmentId, userId);
    }

    @PutMapping("/confirm/{appointmentId}")
    // 删除这一行：@ApiOperation("确认预约")
    public Result confirmAppointment(@PathVariable Integer appointmentId,
                                     @RequestParam Integer serviceAdvisorId) {
        return appointmentService.confirmAppointment(appointmentId, serviceAdvisorId);
    }

    @GetMapping("/owner/{ownerId}")
    // 删除这一行：@ApiOperation("查询车主预约")
    public Result getAppointmentsByOwner(@PathVariable Integer ownerId) {
        return appointmentService.getAppointmentsByOwnerId(ownerId);
    }

    @GetMapping("/advisor/{advisorId}")
    // 删除这一行：@ApiOperation("查询服务顾问的预约")
    public Result getAppointmentsByAdvisor(@PathVariable Integer advisorId) {
        return appointmentService.getAppointmentsByAdvisorId(advisorId);
    }

    @GetMapping("/status/{status}")
    // 删除这一行：@ApiOperation("根据状态查询预约")
    public Result getAppointmentsByStatus(@PathVariable String status) {
        return appointmentService.getAppointmentsByStatus(status);
    }

    @GetMapping("/today")
    // 删除这一行：@ApiOperation("获取今日预约")
    public Result getTodayAppointments() {
        return appointmentService.getTodayAppointments();
    }

    @GetMapping("/list")
    // 删除这一行：@ApiOperation("获取预约列表")
    public Result getAppointmentList() {
        return Result.suc(appointmentService.list());
    }
}