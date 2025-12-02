package com.example.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    // 强制映射数据库字段 total_amount
    @TableField("total_amount")
    private BigDecimal totalAmount;

    private String status;

    private String address;

    // 强制映射数据库字段 receiver_name
    @TableField("receiver_name")
    private String receiverName;

    private LocalDateTime createTime;

    // 业务字段
    @TableField(exist = false)
    private List<CartItem> items;
}