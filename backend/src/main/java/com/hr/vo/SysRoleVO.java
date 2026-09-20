package com.hr.vo;
import lombok.Data;
import java.util.List;

/**
 * 角色出参，额外带出 menuIds（该角色已授权的菜单 id 列表），
 * 前端「分配权限」弹窗用它做默认勾选。
 */
@Data
public class SysRoleVO {
    private Long id;
    private String roleKey;
    private String roleName;
    private String remark;
    private List<Long> menuIds;
}
