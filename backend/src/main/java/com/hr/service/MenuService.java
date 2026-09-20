package com.hr.service;
import com.hr.entity.SysMenu;
import com.hr.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * - getUserMenus：根据当前登录用户，返回他「有权可见」的菜单树（阶段④已有）
 * - getAllMenusTree：返回全部菜单树（含按钮权限点），用于角色授权时勾选（本次新增）
 */
public interface MenuService {

    List<MenuTreeVO> getUserMenus(Long userId);

    /** 查询全部菜单树（含按钮权限点），供角色授权勾选 */
    List<MenuTreeVO> getAllMenusTree();

    /** 菜单管理页：返回完整菜单树，权限 system:menu:view */
    List<MenuTreeVO> getMenuTree();

    /** 新增菜单 */
    void saveMenu(SysMenu menu);

    /** 修改菜单 */
    void updateMenu(SysMenu menu);

    /** 删除菜单（有子节点时禁止） */
    void removeMenu(Long id);
}
