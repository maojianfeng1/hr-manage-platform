package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.Result;
import com.hr.context.UserContext;
import com.hr.entity.SysMenu;
import com.hr.service.MenuService;
import com.hr.vo.MenuTreeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** 当前登录用户可见菜单树（阶段④已有，侧边栏用） */
    @GetMapping("/user-menus")
    public Result<List<MenuTreeVO>> userMenus() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(menuService.getUserMenus(userId));
    }

    /**
     * 全部菜单树（含按钮权限点），供「角色授权」弹窗勾选。
     * 加 @RequiresPermission("system:view")：只有系统管理员能拉。
     */
    @GetMapping("/tree")
    @RequiresPermission("system:view")
    public Result<List<MenuTreeVO>> tree() {
        return Result.success(menuService.getAllMenusTree());
    }

    /**
     * 菜单管理页：返回完整菜单树（含按钮权限点），权限 system:menu:view。
     * 与 /tree 区分：/tree 用于角色授权（system:view），本接口用于菜单自身增删改查（system:menu:view）。
     */
    @GetMapping("/list")
    @RequiresPermission("system:menu:view")
    public Result<List<MenuTreeVO>> list() {
        return Result.success(menuService.getMenuTree());
    }

    /** 新增菜单，权限 system:menu:edit */
    @PostMapping("/save")
    @RequiresPermission("system:menu:edit")
    public Result<?> save(@RequestBody SysMenu menu) {
        menuService.saveMenu(menu);
        return Result.success();
    }

    /** 修改菜单，权限 system:menu:edit */
    @PutMapping("/update")
    @RequiresPermission("system:menu:edit")
    public Result<?> update(@RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return Result.success();
    }

    /** 删除菜单，权限 system:menu:edit；有子节点时返回错误提示 */
    @DeleteMapping("/remove/{id}")
    @RequiresPermission("system:menu:edit")
    public Result<?> remove(@PathVariable Long id) {
        try {
            menuService.removeMenu(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(500, e.getMessage());
        }
    }
}
