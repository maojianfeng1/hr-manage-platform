package com.hr.dto;
import lombok.Data;

/**
 * 新增/编辑角色收参。
 * 角色授权（勾选菜单）走单独的接口，不在本 DTO 里，职责更清晰。
 */
@Data
public class SysRoleSaveDTO {
    private Long id;
    private String roleKey;   // 程序判断用，如 admin / hr / audit，唯一
    private String roleName;  // 展示用，如 系统管理员 / 人事专员
    private String remark;
}
