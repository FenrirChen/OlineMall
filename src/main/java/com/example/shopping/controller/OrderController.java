package com.example.shopping.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.shopping.entity.OrderItem;
import com.example.shopping.entity.Orders;
import com.example.shopping.entity.User;
import com.example.shopping.mapper.OrderItemMapper;
import com.example.shopping.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    // 1. 创建订单
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Orders orderData, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("请先登录");

        try {
            orderService.createOrder(user, orderData);
            return ResponseEntity.ok("订单创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("下单失败: " + e.getMessage());
        }
    }

    // 2. 查看我的订单
    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("请先登录");
        return ResponseEntity.ok(orderService.getByUserId(user.getId()));
    }

    // ---获取单个订单详情 (用于前端弹窗展示) ---
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("未登录");

        Orders order = orderService.getById(id);
        if (order == null) return ResponseEntity.notFound().build();

        // 权限检查：只能看自己的，或者是管理员
        if (!"ADMIN".equals(user.getRole()) && !order.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body("无权查看此订单");
        }

        // 查询明细
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", id));

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);

        return ResponseEntity.ok(result);
    }

    // --- 管理员接口 ---

    // 3. 查看所有订单
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) String status, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body("无权操作");
        }
        return ResponseEntity.ok(orderService.findAll(status));
    }

    // 4. 订单发货
    @PostMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body("无权操作");
        }

        orderService.shipOrder(id);
        return ResponseEntity.ok(Map.of("message", "发货成功"));
    }
}