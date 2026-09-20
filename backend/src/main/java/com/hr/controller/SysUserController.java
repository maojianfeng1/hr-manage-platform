package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.PageResult;
import com.hr.common.result.Result;
import com.hr.dto.SysUserQueryDTO;
import com.hr.dto.SysUserSaveDTO;
import com.hr.service.SysUserService;
import com.hr.vo.SysUserVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    private final SysUserService service;

    public SysUserController(SysUserService service) {
        this.service = service;
    }

    @GetMapping("/page")
    @RequiresPermission("system:user:view")
    public Result<PageResult<SysUserVO>> page(SysUserQueryDTO q) {
        return Result.success(service.page(q));
    }

    @GetMapping("/{id}")
    @RequiresPermission("system:user:view")
    public Result<SysUserVO> detail(@PathVariable Long id) {
        return Result.success(service.detail(id));
    }

    @PostMapping
    @RequiresPermission("system:user:add")
    public Result<?> add(@RequestBody SysUserSaveDTO dto) {
        service.add(dto);
        return Result.success("新增成功");
    }

    @PutMapping
    @RequiresPermission("system:user:add")
    public Result<?> update(@RequestBody SysUserSaveDTO dto) {
        service.update(dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("system:user:add")
    public Result<?> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.success("删除成功");
    }

    /** 重置密码为默认 123456（BCrypt 加密后入库，见 SysUserServiceImpl.DEFAULT_PASSWORD） */
    @PostMapping("/{id}/reset-pwd")
    @RequiresPermission("system:user:add")
    public Result<?> resetPassword(@PathVariable Long id) {
        service.resetPassword(id);
        return Result.success("密码已重置为 123456");
    }
}
