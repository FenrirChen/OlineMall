package com.example.shopping.controller;

import com.example.shopping.entity.Product;
import com.example.shopping.entity.User;
import com.example.shopping.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 获取商品列表 (支持 ?name=xxx 模糊搜索)
    @GetMapping
    public List<Product> list(@RequestParam(required = false) String name) {
        if (name != null && !name.isEmpty()) {
            return productService.searchByName(name);
        }
        return productService.findAll();
    }

    // 获取单个商品详情
    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Long id) {
        Product product = productService.getById(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    // --- 以下为管理员接口 ---

    // 新增或更新商品
    @PostMapping
    public ResponseEntity<?> save(@RequestBody Product product, HttpSession session) {
        // 简单的权限检查
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body("无权操作");
        }

        productService.saveOrUpdate(product);
        return ResponseEntity.ok("保存成功");
    }

    // 删除商品
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body("无权操作");
        }

        productService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }
}
