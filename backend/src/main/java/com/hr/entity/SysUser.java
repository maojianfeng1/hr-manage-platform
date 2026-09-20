package com.hr.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户（对应 sys_user 表）
 *
 * 知识点：实体类字段用包装类型 Long / Integer 而不是 long / int。
 * 原因：数据库里这些列允许为 NULL（如 employee_id 普通管理员可空），
 * 用基本类型会 NPE 或拿到 0 这种「假值」，包装类型才能正确表达「没有值」。
 */
@Data
public class SysUser implements Serializable {

    private Long id;
    private String username;
    private String password;        // BCrypt 哈希，绝不明文
    private String realName;
    private String avatar;
    private String email;
    private String phone;
    private Integer status;         // 1 启用 0 停用
    private Long employeeId;        // 关联 hr_employee.id，普通员工必填
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
