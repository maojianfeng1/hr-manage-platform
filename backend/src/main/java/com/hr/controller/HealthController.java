package com.hr.controller;

import com.hr.common.result.Result;
import com.hr.mapper.SystemMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查接口：用于阶段③验证「Web + MyBatis + MySQL」整条链路是否打通
 * 验证地址：http://localhost:8080/api/health
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final SystemMapper systemMapper;

    public HealthController(SystemMapper systemMapper) {
        this.systemMapper = systemMapper;
    }

    @GetMapping
    public Result<Map<String, Object>> health() {
        // 1 = 表示数据库能正常执行 SELECT 1，说明数据源 + 连接池是通的
        Integer dbPing = systemMapper.ping();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("time", LocalDateTime.now());
        data.put("db", dbPing != null && dbPing == 1 ? "connected" : "unknown");
        data.put("dbPing", dbPing);
        return Result.success("服务运行正常", data);
    }
}
