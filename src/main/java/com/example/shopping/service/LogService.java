package com.example.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shopping.entity.SysLog;
import com.example.shopping.mapper.SysLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService extends ServiceImpl<SysLogMapper, SysLog> {

    // 记录日志
    public void record(String username, String type, String details) {
        // 可以在这里获取 IP
        SysLog log = new SysLog();
        log.setUserUsername(username);
        log.setActionType(type);
        log.setDetails(details);
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }

    // 查询日志
    public List<SysLog> findLogs(String type) {
        QueryWrapper<SysLog> query = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            query.eq("action_type", type);
        }
        query.orderByDesc("create_time");
        return this.list(query);
    }
}