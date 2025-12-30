package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String appointmentNo;   // 预约号
    private Integer vehicleId;      // 车辆ID
    private Integer ownerId;        // 车主ID
    private Integer serviceAdvisorId; // 服务顾问ID
    private String serviceType;     // 服务类型：maintenance(保养), repair(维修), inspection(检测)
    private LocalDateTime startTime;    // 预约开始时间
    private LocalDateTime endTime;      // 预约结束时间
    private String status;          // 状态：pending(待确认), confirmed(已确认), canceled(已取消), completed(已完成)
    private String remark;          // 备注
    private LocalDateTime createTime;

    // 添加注解，告诉MyBatis Plus这个字段不在数据库表中
    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Vehicle vehicle;        // 车辆信息
    @TableField(exist = false)
    private User owner;             // 车主信息
    @TableField(exist = false)
    private User serviceAdvisor;    // 服务顾问信息
}