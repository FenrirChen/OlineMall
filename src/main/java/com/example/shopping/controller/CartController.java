package com.example.shopping.controller;

import com.example.shopping.entity.CartItem;
import com.example.shopping.entity.User;
import com.example.shopping.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 获取当前用户的购物车
    @GetMapping
    public ResponseEntity<?> getMyCart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("请先登录");

        List<CartItem> items = cartService.getCartByUserId(user.getId());
        return ResponseEntity.ok(items);
    }

    // 添加商品到购物车 (首页使用)
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("请先登录");

        Long productId = Long.valueOf(payload.get("productId").toString());
        Integer quantity = Integer.valueOf(payload.get("quantity").toString());

        cartService.addToCart(user.getId(), productId, quantity);
        return ResponseEntity.ok("添加成功");
    }

    // 更新购物车数量 (购物车页面使用)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("请先登录");

        Integer quantity = payload.get("quantity");
        cartService.updateQuantity(user.getId(), id, quantity);
        return ResponseEntity.ok("更新成功");
    }

    // 移除购物车项
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("请先登录");

        cartService.removeById(id);
        return ResponseEntity.ok("移除成功");
    }
}