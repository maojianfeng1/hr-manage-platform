package com.hr.dto;
import lombok.Data;
import java.util.List;

/**
 * 新增/编辑用户收参。
 *
 * 知识点：为什么用 DTO 而不是拿 SysUser 实体直接收 @RequestBody？
 * 1. 实体里有 password / isDeleted 等敏感/内部字段，直接用实体收参等于把「改密码、改删除标记」
 *    的入口也暴露给前端，风险大；
 * 2. 我们需要一个「实体没有」的字段 roleIds（用户绑定哪些角色），实体装不下；
 * 3. 用独立的收参对象，能精准控制「前端允许传什么」，多一层保护。
 * 这也是为什么业务模块的增改都倾向用 DTO/实体分离。
 */
@Data
public class SysUserSaveDTO {
    private Long id;            // 有值=编辑，无值=新增
    private String username;    // 登录账号（编辑时锁定不可改）
    private String realName;
    private String email;
    private String phone;
    private Integer status;     // 1 启用 0 停用
    private Long employeeId;    // 关联员工档案（可选）
    private List<Long> roleIds; // 绑定的角色 id 列表
}
