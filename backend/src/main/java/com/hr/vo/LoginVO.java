package com.hr.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录成功后的返回结构
 *
 * 前端拿到这个对象后会做三件事：
 * 1. token → 存 localStorage，后续每个请求塞进 Authorization 头
 * 2. roles → 存 Pinia，用于路由/菜单级判断
 * 3. perms → 存 Pinia，用于按钮级显隐（v-if="hasPerm('emp:edit')"）
 *
 * 注意：把 roles / perms 一次下发，前端就不用再单独请求一次「查我的权限」，
 * 减少一次往返。但如果权限数据量很大，更好的做法是拆成独立的 /auth/info 接口按需拉取。
 */
@Data
public class LoginVO implements Serializable {

    /** JWT 令牌 */
    private String token;

    /** 令牌类型，固定 Bearer（前端拼成 Authorization: Bearer xxx） */
    private String tokenType = "Bearer";

    /** 有效期（秒），前端可用于做「即将过期」提示 */
    private Long expiresIn;

    private UserInfoVO userInfo;

    /** 角色标识集合，如 ["admin"] */
    private List<String> roles;

    /** 权限标识集合，如 ["emp:edit", "sal:payroll:edit"] */
    private List<String> perms;
}
