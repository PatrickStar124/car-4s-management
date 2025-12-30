package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)  // 所有实体类都改成AUTO
    private Integer id;

    private String orderNo;         // 工单编号
    private Integer vehicleId;      // 车辆ID
    private Integer ownerId;        // 车主ID
    private Integer serviceAdvisorId; // 服务顾问ID
    private Integer mechanicId;     // 维修技师ID（可空）
    private String status;          // 状态：pending(待接车), inspecting(检查中), quote_pending(待确认报价), repairing(维修中), completed(已完成), delivered(已交车)
    private String problemDesc;     // 问题描述
    private BigDecimal estimatedAmount; // 预估金额
    private BigDecimal actualAmount;    // 实际金额
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;          // 备注

    @TableField(exist = false)
    private Vehicle vehicle;        // 车辆信息
    @TableField(exist = false)
    private User owner;             // 车主信息
    @TableField(exist = false)
    private User serviceAdvisor;    // 服务顾问信息
    @TableField(exist = false)
    private User mechanic;          // 维修技师信息
}