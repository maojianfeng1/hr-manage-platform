package com.hr.vo;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户出参。基于「按需裁剪」原则，绝不把 SysUser（含 password）直接序列化给前端。
 * roleIds 单独带出，前端再用角色列表映射成中文名展示（与部门页把 leaderId 翻译成姓名同思路）。
 */
@Data
public class SysUserVO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Integer status;
    private Long employeeId;
    private List<Long> roleIds;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
}
