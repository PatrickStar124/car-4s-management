package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@TableName("vehicle")
public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)  // 所有实体类都改成AUTO
    private Integer id;

    private String plateNumber;     // 车牌号
    private String vin;             // 车架号
    private String brand;           // 品牌
    private String model;           // 型号
    private Integer ownerId;        // 车主ID（关联User）
    private LocalDate purchaseDate; // 购买日期
    private Integer mileage;        // 当前里程
    private String color;           // 颜色

    @TableField(exist = false)
    private User owner;             // 车主信息（关联查询用）
}