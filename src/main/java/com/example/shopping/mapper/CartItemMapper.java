package com.example.shopping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shopping.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    // 自定义 SQL：关联 Product 表查询，一次性获取商品详情
    // 将 p.name 映射到 CartItem.productName, p.image_url 映射到 CartItem.productImage
    @Select("SELECT c.*, p.name as product_name, p.image_url as product_image, p.price as price " +
            "FROM cart_item c " +
            "LEFT JOIN product p ON c.product_id = p.id " +
            "WHERE c.user_id = #{userId}")
    List<CartItem> selectListWithProductInfo(Long userId);
}