package com.hr.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统级 Mapper，不对应具体业务表，仅用于连通性探测。
 * 阶段④开始会新增真正的业务 Mapper（sys_user / hr_employee 等）。
 */
@Mapper
public interface SystemMapper {

     //探测数据库连接：能返回 1 即代表 MySQL 可达
    @Select("SELECT 1")
    Integer ping();
}
