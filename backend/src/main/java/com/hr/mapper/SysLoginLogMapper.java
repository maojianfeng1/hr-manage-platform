package com.hr.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

//登录日志 Mapper（演示「操作日志」这一类写表操作）
@Mapper
public interface SysLoginLogMapper {

//     插入一条登录日志
    int insert(@Param("username") String username,
               @Param("ip") String ip,
               @Param("status") Integer status,
               @Param("msg") String msg);
}
