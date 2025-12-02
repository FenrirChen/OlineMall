package com.example.shopping.controller;

import com.example.shopping.entity.User;
import com.example.shopping.service.LogService;
import com.example.shopping.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogService logService; // 注入日志服务

    // 登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser, HttpSession session) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
        if (user != null) {
            session.setAttribute("user", user);

            // --- 记录登录日志 ---
            logService.record(user.getUsername(), "LOGIN", "用户登录系统");

            return ResponseEntity.ok(Map.of("message", "登录成功", "user", user));
        }
        return ResponseEntity.status(401).body(Map.of("message", "用户名或密码错误"));
    }

    // 注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userService.register(user);
            return ResponseEntity.ok(Map.of("message", "注册成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 注销
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "注销成功"));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body(Map.of("message", "未登录"));
    }
}