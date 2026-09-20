package com.hr.vo;

import lombok.Data;

import java.util.List;

/**
 * 当前登录用户档案（/api/auth/info 返回）
 *
 * 相比登录时的 UserInfoVO，这里额外带了 roles / perms ——
 * 因为前端刷新页面后只用 token 调 /info 重新拉取，
 * 必须一次性把「用户基本信息 + 角色 + 权限」都给全，否则刷新后权限会丢。
 */
@Data
public class UserProfileVO {
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private Long employeeId;
    private List<String> roles;
    private List<String> perms;
}
