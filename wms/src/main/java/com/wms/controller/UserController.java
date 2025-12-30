package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.User;
import com.wms.service.UserService;
// 删除下面两行
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")// "用户管理"
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")    // ("用户登录")
    public Result login(@RequestParam String no, @RequestParam String password) {
        return userService.login(no, password);
    }

    @PostMapping("/register")   // ("用户注册")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }

    @GetMapping("/info/{id}")// ("获取用户信息")
    public Result getUserInfo(@PathVariable Integer id) {
        User user = userService.getUserInfo(id);
        return Result.suc(user);
    }

    @GetMapping("/role/{role}")    // ("根据角色查询用户")
    public Result getUsersByRole(@PathVariable String role) {
        return userService.getUsersByRole(role);
    }

    @PostMapping("/staff")    // ("创建员工账号（管理员）")
    public Result createStaff(@RequestBody User user) {
        return userService.createStaff(user);
    }

    @PutMapping("/role/{userId}") // ("修改用户角色")
    public Result updateUserRole(@PathVariable Integer userId, @RequestParam String role) {
        return userService.updateUserRole(userId, role);
    }

    @GetMapping("/service-advisors") // ("获取所有服务顾问")
    public Result getServiceAdvisors() {
        List<User> advisors = userService.getServiceAdvisors();
        return Result.suc(advisors);
    }

    @GetMapping("/mechanics") // ("获取所有维修技师")
    public Result getMechanics() {
        List<User> mechanics = userService.getMechanics();
        return Result.suc(mechanics);
    }

    @GetMapping("/list")//("获取用户列表")
    public Result getUserList() {
        List<User> users = userService.list();
        return Result.suc(users);
    }

    @PutMapping("/update") //("更新用户信息")
    public Result updateUser(@RequestBody User user) {
        boolean updated = userService.updateById(user);
        return updated ? Result.suc("更新成功") : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")//("删除用户（逻辑删除）")
    public Result deleteUser(@PathVariable Integer id) {
        User user = new User();
        user.setId(id);
        user.setIsValid("N");
        boolean deleted = userService.updateById(user);
        return deleted ? Result.suc("删除成功") : Result.fail("删除失败");
    }
}