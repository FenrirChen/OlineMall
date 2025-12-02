package com.example.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shopping.entity.Product;
import com.example.shopping.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    // 模糊搜索
    public List<Product> searchByName(String name) {
        QueryWrapper<Product> query = new QueryWrapper<>();
        query.like("name", name);
        return this.list(query);
    }

    // 全查
    public List<Product> findAll() {
        return this.list();
    }

    public void deleteById(Long id) {
        // 调用 MyBatis-Plus 默认的 removeById() 方法
        this.removeById(id);
    }
}