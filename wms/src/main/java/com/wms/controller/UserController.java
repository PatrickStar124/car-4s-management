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
 * 接口排序逻辑：身份验证 → 基础查询 → 角色专属查询 → 核心操作 → 管理操作
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // ======================== 身份验证接口（登录/注册）========================
    /**
     * 用户登录接口
     * 用途：所有用户（车主/员工/管理员）登录系统，获取访问凭证
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
     * 用途：普通用户（如车主）自主注册账号
     * @param user 注册用户信息实体（包含账号、密码、角色等核心信息）
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }

    // ======================== 基础查询接口（通用/单条/全量）========================
    /**
     * 根据用户ID获取用户详情信息
     * 用途：查看单个用户的完整信息（如个人中心、账号详情）
     * @param id 用户ID
     * @return 包含用户完整信息的操作结果
     */
    @GetMapping("/info/{id}")
    public Result getUserInfo(@PathVariable Integer id) {
        User user = userService.getUserInfo(id);
        return Result.suc(user);
    }

    /**
     * 获取所有用户列表
     * 用途：管理员后台查看系统内所有用户数据
     * @return 全量用户信息列表
     */
    @GetMapping("/list")
    public Result getUserList() {
        List<User> users = userService.list();
        return Result.suc(users);
    }

    /**
     * 根据角色查询用户列表
     * 用途：按角色批量筛选用户（如筛选所有车主/所有管理员）
     * @param role 用户角色（如：车主、服务顾问、维修技师、管理员等）
     * @return 对应角色的用户列表
     */
    @GetMapping("/role/{role}")
    public Result getUsersByRole(@PathVariable String role) {
        return userService.getUsersByRole(role);
    }

    // ======================== 角色专属查询接口（常用角色快捷查询）========================
    /**
     * 获取所有服务顾问列表
     * 用途：预约、派单等场景快速获取服务顾问名单（快捷接口，无需传角色参数）
     * @return 服务顾问用户列表
     */
    @GetMapping("/service-advisors")
    public Result getServiceAdvisors() {
        List<User> advisors = userService.getServiceAdvisors();
        return Result.suc(advisors);
    }

    /**
     * 获取所有维修技师列表
     * 用途：派工、维修单分配等场景快速获取技师名单（快捷接口）
     * @return 维修技师用户列表
     */
    @GetMapping("/mechanics")
    public Result getMechanics() {
        List<User> mechanics = userService.getMechanics();
        return Result.suc(mechanics);
    }

    // ======================== 核心操作接口（信息修改/账号创建）========================
    /**
     * 更新用户信息
     * 用途：用户修改个人信息（如昵称、联系方式）或管理员修改用户基础信息
     * @param user 待更新的用户信息（需包含用户ID）
     * @return 操作结果（成功/失败提示）
     */
    @PutMapping("/update")
    public Result updateUser(@RequestBody User user) {
        boolean updated = userService.updateById(user);
        return updated ? Result.suc("更新成功") : Result.fail("更新失败");
    }

    /**
     * 修改用户角色
     * 用途：管理员调整用户的系统角色（如将普通员工设为服务顾问）
     * @param userId 用户ID
     * @param role 新角色（如：服务顾问、维修技师等）
     * @return 角色修改结果
     */
    @PutMapping("/role/{userId}")
    public Result updateUserRole(@PathVariable Integer userId, @RequestParam String role) {
        return userService.updateUserRole(userId, role);
    }

    /**
     * 管理员创建员工账号
     * 用途：管理员批量创建内部员工账号（服务顾问/技师等），区别于普通用户注册
     * @param user 员工账号信息（需包含角色、账号、密码等）
     * @return 账号创建结果
     */
    @PostMapping("/staff")
    public Result createStaff(@RequestBody User user) {
        return userService.createStaff(user);
    }

    // ======================== 管理操作接口（删除）========================
    /**
     * 逻辑删除用户（修改用户有效状态为无效）
     * 用途：管理员禁用/删除用户账号，保留数据不物理删除
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