package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.User;
import com.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 用户管理控制器
 * 处理用户相关的接口请求，包括登录、注册、信息查询、角色管理、员工账号创建等操作
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     * @param no 用户编号/账号
     * @param password 用户密码
     * @return 登录结果（包含token/用户信息或失败提示）
     */
    @PostMapping("/login")
    public Result login(@RequestParam String no, @RequestParam String password) {
        return userService.login(no, password);
    }

    /**
     * 用户注册接口
     * @param user 注册用户信息实体（包含账号、密码、角色等核心信息）
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }

    /**
     * 根据用户ID获取用户详情信息
     * @param id 用户ID
     * @return 包含用户完整信息的操作结果
     */
    @GetMapping("/info/{id}")
    public Result getUserInfo(@PathVariable Integer id) {
        User user = userService.getUserInfo(id);
        return Result.suc(user);
    }

    /**
     * 根据角色查询用户列表
     * @param role 用户角色（如：车主、服务顾问、维修技师、管理员等）
     * @return 对应角色的用户列表
     */
    @GetMapping("/role/{role}")
    public Result getUsersByRole(@PathVariable String role) {
        return userService.getUsersByRole(role);
    }

    /**
     * 管理员创建员工账号
     * @param user 员工账号信息（需包含角色、账号、密码等）
     * @return 账号创建结果
     */
    @PostMapping("/staff")
    public Result createStaff(@RequestBody User user) {
        return userService.createStaff(user);
    }

    /**
     * 修改用户角色
     * @param userId 用户ID
     * @param role 新角色（如：服务顾问、维修技师等）
     * @return 角色修改结果
     */
    @PutMapping("/role/{userId}")
    public Result updateUserRole(@PathVariable Integer userId, @RequestParam String role) {
        return userService.updateUserRole(userId, role);
    }

    /**
     * 获取所有服务顾问列表
     * @return 服务顾问用户列表
     */
    @GetMapping("/service-advisors")
    public Result getServiceAdvisors() {
        List<User> advisors = userService.getServiceAdvisors();
        return Result.suc(advisors);
    }

    /**
     * 获取所有维修技师列表
     * @return 维修技师用户列表
     */
    @GetMapping("/mechanics")
    public Result getMechanics() {
        List<User> mechanics = userService.getMechanics();
        return Result.suc(mechanics);
    }

    /**
     * 获取所有用户列表
     * @return 全量用户信息列表
     */
    @GetMapping("/list")
    public Result getUserList() {
        List<User> users = userService.list();
        return Result.suc(users);
    }

    /**
     * 更新用户信息
     * @param user 待更新的用户信息（需包含用户ID）
     * @return 操作结果（成功/失败提示）
     */
    @PutMapping("/update")
    public Result updateUser(@RequestBody User user) {
        boolean updated = userService.updateById(user);
        return updated ? Result.suc("更新成功") : Result.fail("更新失败");
    }

    /**
     * 逻辑删除用户（修改用户有效状态为无效）
     * @param id 用户ID
     * @return 操作结果（成功/失败提示）
     */
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Integer id) {
        User user = new User();
        user.setId(id);
        user.setIsValid("N");
        boolean deleted = userService.updateById(user);
        return deleted ? Result.suc("删除成功") : Result.fail("删除失败");
    }
}