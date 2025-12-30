package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)  // 所有实体类都改成AUTO
    private Integer id;

    private Integer partId;         // 配件ID
    private Integer warehouseId;    // 仓库ID（简单实现可先固定为1）
    private Integer stockQuantity;  // 库存数量
    private Integer safeStock;      // 安全库存
    private Integer lockedQuantity; // 锁定数量（已预约未出库）
    private LocalDateTime lastInboundTime;  // 最后入库时间
    private LocalDateTime lastOutboundTime; // 最后出库时间

    @TableField(exist = false)
    private Part part;              // 配件信息
}