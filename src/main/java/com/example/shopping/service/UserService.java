package com.example.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shopping.entity.User;
import com.example.shopping.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    // 登录逻辑
    public User login(String username, String password) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", username).eq("password", password);
        return this.getOne(query);
    }

    // 注册逻辑
    public void register(User user) {
        // 检查用户名是否存在
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", user.getUsername());
        if (this.count(query) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        user.setCreateTime(LocalDateTime.now());
        // 如果前端没传角色，默认是顾客
        if (user.getRole() == null) {
            user.setRole("CUSTOMER");
        }
        this.save(user);
    }

    public List<User> findAll() {
        // 调用 MyBatis-Plus 默认的 list() 方法
        return this.list();
    }
}