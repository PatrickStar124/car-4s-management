// UserService.java
package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.User;
import com.wms.common.Result;

import java.util.List;

public interface UserService extends IService<User> {

    Result login(String no, String password);

    Result register(User user);

    User getUserInfo(Integer id);

    // =========== 新增4S店系统专用方法 ===========

    /**
     * 根据角色查询用户列表
     */
    Result getUsersByRole(String role);

    /**
     * 创建员工账号（管理员用）
     */
    Result createStaff(User user);

    /**
     * 修改用户角色
     */
    Result updateUserRole(Integer userId, String role);

    /**
     * 获取所有服务顾问
     */
    List<User> getServiceAdvisors();

    /**
     * 获取所有维修技师
     */
    List<User> getMechanics();

}