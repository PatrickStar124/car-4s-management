package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("part")
public class Part implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String partNo;          // 配件编号
    private String partName;        // 配件名称
    private String brand;           // 品牌
    private String model;           // 适用车型
    private BigDecimal purchasePrice; // 采购价
    private BigDecimal salePrice;   // 销售价
    private String unit;            // 单位（个、套、升等）
    private String category;        // 分类（机油、滤清器、刹车片等）

    // 添加这个字段
    @TableField(exist = false)
    private Inventory inventory;    // 库存信息（关联查询用）
}