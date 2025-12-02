package com.example.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userUsername; // 记录操作人的用户名

    private String actionType;   // LOGIN, VIEW, ORDER

    private String details;      // 具体描述

    private String ipAddress;

    private LocalDateTime createTime;
}