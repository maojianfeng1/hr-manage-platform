package com.hr.dto;
import lombok.Data;

/**
 * 用户分页查询条件。
 * 字段名与前端 query 对象一一对应（page/size/username/realName/status）。
 */
@Data
public class SysUserQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String username;   // 模糊
    private String realName;   // 模糊
    private Integer status;    // 1 启用 0 停用
}
