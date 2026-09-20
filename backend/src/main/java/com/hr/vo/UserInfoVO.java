package com.hr.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户的基础信息（仅含前端展示所需字段，不含密码等敏感字段）
 *
 * 知识点：返回给前端时「按需裁剪」，绝不要把整个 SysUser 实体直接序列化出去。
 * 实体里有 password / isDeleted 等字段，一旦误序列化就是严重泄漏。
 * 用独立的 VO（View Object）做「出参裁剪」是后端基本功。
 */
@Data
public class UserInfoVO implements Serializable {

    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private Long employeeId;
}
