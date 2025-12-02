package com.example.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    //使用 BigDecimal 处理金额
    private BigDecimal price;

    private Integer stock;

    private String imageUrl;

    private String description;

    private LocalDateTime createTime;
}