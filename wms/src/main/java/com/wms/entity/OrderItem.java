package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;        // 工单ID
    private String itemType;        // 项目类型：labor(工时), part(配件), other(其他)
    private String description;     // 项目描述
    private Integer quantity;       // 数量
    private BigDecimal unitPrice;   // 单价
    private BigDecimal totalPrice;  // 总价
    private Integer partId;         // 如果是配件，关联配件ID（可空）

    @TableField(exist = false)
    private Part part;              // 配件信息（关联查询用）这里应该是 com.wms.entity.Part
}