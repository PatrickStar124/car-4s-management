package com.wms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "✅ 4S店管理系统API正常运行！时间：" + new java.util.Date();
    }

    @GetMapping("/")
    public String index() {
        // 添加字符编码声明
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
                "<h1>🚗 欢迎使用4S店数字化管理系统！</h1>" +
                "<p>👉 用户API: /api/user/*</p>" +
                "<p>👉 车辆API: /api/vehicle/*</p>" +
                "<p>👉 工单API: /api/repair-order/*</p>" +
                "<p>👉 预约API: /api/appointment/*</p>" +
                "<p>⏰ 时间：" + new java.util.Date() + "</p>" +
                "</body></html>";
    }
}