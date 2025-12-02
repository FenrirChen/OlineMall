package com.example.shopping.controller;

import com.example.shopping.entity.User;
import com.example.shopping.service.LogService;
import com.example.shopping.service.StatsService;
import com.example.shopping.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private StatsService statsService;

    @Autowired
    private LogService logService;

    @Autowired
    private UserService userService;

    // 权限检查辅助方法
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }

    // 1. 获取仪表盘统计数据
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).build();

        Map<String, Object> data = new HashMap<>();
        data.put("totalSales", statsService.getTotalSales());
        data.put("totalOrders", statsService.getTotalOrdersCount());
        data.put("totalUsers", statsService.getTotalUsersCount());
        data.put("topProducts", statsService.getTopSellingProducts(5));

        // 返回真实报表数据
        data.put("dailyReport", statsService.getRecentSalesStats(7));

        return ResponseEntity.ok(data);
    }

    // 2.Excel 导出接口
    // 注意：这里返回 void，因为通过 response 流直接输出文件
    @GetMapping("/export")
    public void exportReport(HttpServletResponse response, HttpSession session) throws IOException {
        // 鉴权：只有管理员能导出
        if (!isAdmin(session)) {
            response.sendError(403, "无权操作");
            return;
        }

        // 设置响应头，告诉浏览器下载文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // attachment 表示下载，filename 指定默认文件名
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.xlsx");

        // 调用 Service 生成文件并写入响应流
        // 这一步会把 Excel 的二进制数据直接发给浏览器
        statsService.exportDailyReport(response.getOutputStream());
    }

    // 3. 获取用户列表
    @GetMapping("/users")
    public ResponseEntity<?> getUserList(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.findAll());
    }

    // 4. 获取系统日志
    @GetMapping("/logs")
    public ResponseEntity<?> getSystemLogs(@RequestParam(required = false) String type, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(logService.findLogs(type));
    }
}