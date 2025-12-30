package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("user")  // MySQL不需要双引号，直接写表名
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)  // MySQL可以用AUTO自增
    private Integer id;

    private String no;          // 工号/用户编号
    private String name;        // 姓名
    private String password;    // 密码
    private String role;        // 角色（owner车主, sales销售顾问, service服务顾问, mechanic维修技师, warehouse仓库管理员, admin管理员）
    private String phone;       // 联系电话
    private String email;       // 邮箱
    private String isValid = "Y";
}