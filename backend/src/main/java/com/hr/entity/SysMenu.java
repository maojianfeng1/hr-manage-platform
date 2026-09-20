package com.hr.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 菜单 / 按钮权限（对应 sys_menu 表）
 *
 * 知识点：这是整个 RBAC 最核心的一张表。
 * - menu_type = 1：菜单（页面），前端据此生成侧边栏
 * - menu_type = 2：按钮（权限点），前端据此控制按钮显隐
 * - perms：权限标识字符串（如 emp:edit），后端 @RequiresPermission("emp:edit") 就是拿它比对
 *
 * 「菜单」和「按钮权限」共用一张表，是业界最常见的设计（区别于 menu + permission 拆两张表），
 * 好处是树形结构统一、授权逻辑统一。
 */
@Data
public class SysMenu implements Serializable {

    private Long id;

    /** 父菜单 ID，0 表示顶级 */
    private Long parentId;

    private String menuName;

    /** 1菜单 2按钮 */
    private Integer menuType;

    /** 权限标识，如 emp:edit、sal:payroll:edit */
    private String perms;

    /** 前端路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    private String icon;

    private Integer sort;
}
