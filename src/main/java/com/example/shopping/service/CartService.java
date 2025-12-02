package com.example.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shopping.entity.CartItem;
import com.example.shopping.mapper.CartItemMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService extends ServiceImpl<CartItemMapper, CartItem> {

    // 获取购物车（包含商品详细信息）
    public List<CartItem> getCartByUserId(Long userId) {
        return baseMapper.selectListWithProductInfo(userId);
    }

    // 添加商品到购物车 (累加逻辑)
    public void addToCart(Long userId, Long productId, Integer quantity) {
        QueryWrapper<CartItem> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("product_id", productId);
        CartItem existItem = this.getOne(query);

        if (existItem != null) {
            // 如果已存在，则在原有基础上增加
            existItem.setQuantity(existItem.getQuantity() + quantity);
            this.updateById(existItem);
        } else {
            // 如果不存在，新增
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setCreateTime(LocalDateTime.now());
            this.save(newItem);
        }
    }

    // 直接更新数量 (用于购物车页面的加减操作)
    public void updateQuantity(Long userId, Long cartItemId, Integer newQuantity) {
        if (newQuantity <= 0) {
            // 如果数量小于等于0，直接删除
            this.removeById(cartItemId);
            return;
        }

        CartItem item = this.getById(cartItemId);
        if (item != null && item.getUserId().equals(userId)) {
            item.setQuantity(newQuantity);
            this.updateById(item);
        }
    }

    // 清空某用户的购物车
    public void clearCart(Long userId) {
        QueryWrapper<CartItem> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        this.remove(query);
    }
}