package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.common.Result;
import com.wms.entity.User;
import com.wms.mapper.UserMapper;
import com.wms.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 定义角色常量
    private static final String ROLE_OWNER = "owner";        // 车主
    private static final String ROLE_SERVICE = "service";    // 服务顾问
    private static final String ROLE_MECHANIC = "mechanic";  // 维修技师
    private static final String ROLE_WAREHOUSE = "warehouse";// 仓库管理员
    private static final String ROLE_ADMIN = "admin";        // 管理员

    @Override
    public Result login(String no, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getNo, no)
                .eq(User::getPassword, password)
                .eq(User::getIsValid, "Y");

        User loginUser = this.getOne(wrapper);
        if (loginUser != null) {
            // 记录登录日志（可选）
            // logService.saveLog(loginUser.getId(), "用户登录");

            // 根据角色返回不同的菜单权限（后续扩展）
            return Result.suc(loginUser);
        }
        return Result.fail("用户名或密码错误");
    }

    @Override
    public Result register(User user) {
        // 1. 校验必填字段
        if (user.getNo() == null || user.getNo().trim().isEmpty()) {
            return Result.fail("工号不能为空");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return Result.fail("姓名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }

        // 2. 检查工号是否已存在
        if (this.checkUserExists(user.getNo())) {
            return Result.fail("工号已存在");
        }

        // 3. 设置默认值
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole(ROLE_OWNER); // 默认注册为车主角色
        }
        if (user.getIsValid() == null) {
            user.setIsValid("Y");
        }

        // 4. 密码加密（建议添加）
        // user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 5. 保存用户
        boolean saved = this.save(user);
        return saved ? Result.suc("注册成功", user) : Result.fail("注册失败");
    }

    @Override
    public User getUserInfo(Integer id) {
        return this.getById(id);
    }

    @Override
    public Result getUsersByRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return Result.fail("角色参数不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, role)
                .eq(User::getIsValid, "Y");
        List<User> users = this.list(wrapper);

        return Result.suc(users);
    }

    @Override
    public Result createStaff(User user) {
        // 创建员工账号（需要管理员权限，这里先做基础校验）

        // 1. 校验角色
        String role = user.getRole();
        if (!isValidStaffRole(role)) {
            return Result.fail("无效的员工角色，必须是：service, mechanic, warehouse 之一");
        }

        // 2. 检查工号
        if (this.checkUserExists(user.getNo())) {
            return Result.fail("工号已存在");
        }

        // 3. 设置默认值
        user.setIsValid("Y");

        // 4. 保存
        boolean saved = this.save(user);
        return saved ? Result.suc("员工账号创建成功", user) : Result.fail("创建失败");
    }

    @Override
    public Result updateUserRole(Integer userId, String role) {
        if (!isValidRole(role)) {
            return Result.fail("无效的角色");
        }

        User user = this.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        user.setRole(role);
        boolean updated = this.updateById(user);
        return updated ? Result.suc("角色更新成功") : Result.fail("更新失败");
    }

    @Override
    public List<User> getServiceAdvisors() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, ROLE_SERVICE)
                .eq(User::getIsValid, "Y");
        return this.list(wrapper);
    }

    @Override
    public List<User> getMechanics() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, ROLE_MECHANIC)
                .eq(User::getIsValid, "Y");
        return this.list(wrapper);
    }

    private boolean checkUserExists(String no) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getNo, no);
        return this.count(wrapper) > 0;
    }

    private boolean isValidRole(String role) {
        return ROLE_OWNER.equals(role) ||
                ROLE_SERVICE.equals(role) ||
                ROLE_MECHANIC.equals(role) ||
                ROLE_WAREHOUSE.equals(role) ||
                ROLE_ADMIN.equals(role);
    }

    private boolean isValidStaffRole(String role) {
        return ROLE_SERVICE.equals(role) ||
                ROLE_MECHANIC.equals(role) ||
                ROLE_WAREHOUSE.equals(role);
    }
}