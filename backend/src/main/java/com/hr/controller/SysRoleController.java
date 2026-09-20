package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.Result;
import com.hr.dto.SysRoleSaveDTO;
import com.hr.vo.SysRoleVO;
import com.hr.service.SysRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    private final SysRoleService service;

    public SysRoleController(SysRoleService service) {
        this.service = service;
    }

    @GetMapping("/list")
    @RequiresPermission("system:role:view")
    public Result<List<SysRoleVO>> list() {
        return Result.success(service.listAll());
    }

    @GetMapping("/{id}")
    @RequiresPermission("system:role:view")
    public Result<SysRoleVO> detail(@PathVariable Long id) {
        return Result.success(service.detail(id));
    }

    @PostMapping
    @RequiresPermission("system:role:edit")
    public Result<?> add(@RequestBody SysRoleSaveDTO dto) {
        service.add(dto);
        return Result.success("新增成功");
    }

    @PutMapping
    @RequiresPermission("system:role:edit")
    public Result<?> update(@RequestBody SysRoleSaveDTO dto) {
        service.update(dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("system:role:edit")
    public Result<?> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.success("删除成功");
    }

    /** 给角色授权菜单：前端传菜单 id 数组 [1,2,3] 作为请求体 */
    @PostMapping("/{id}/menus")
    @RequiresPermission("system:role:edit")
    public Result<?> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        service.assignMenus(id, menuIds);
        return Result.success("授权成功");
    }
}
