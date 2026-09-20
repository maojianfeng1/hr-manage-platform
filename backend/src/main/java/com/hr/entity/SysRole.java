package com.hr.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色（对应 sys_role 表）
 *
 * role_key 是「程序判断用」的英文标识（admin / hr / employee），
 * role_name 是「展示给用户看」的中文名（系统管理员 / 人事专员 / 普通员工）。
 * 业务代码里判断角色一律用 role_key，绝不用中文名 —— 中文名随时可能改文案。
 */
@Data
public class SysRole implements Serializable {

    private Long id;

    /** 角色标识：admin / hr / employee */
    private String roleKey;

    /** 角色名称：系统管理员 / 人事专员 / 普通员工 */
    private String roleName;

    private String remark;
}
