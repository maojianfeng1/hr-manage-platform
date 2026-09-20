package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.PageResult;
import com.hr.common.result.Result;
import com.hr.dto.HrEmployeeQueryDTO;
import com.hr.entity.HrEmployee;
import com.hr.service.HrEmployeeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
public class HrEmployeeController {

    private final HrEmployeeService service;
    public HrEmployeeController(HrEmployeeService service) { this.service = service; }

    @GetMapping("/page")
    @RequiresPermission("emp:view")
    public Result<PageResult<HrEmployee>> page(HrEmployeeQueryDTO q) {
        return Result.success(service.page(q));
    }

    @GetMapping("/{id}")
    @RequiresPermission("emp:view")
    public Result<HrEmployee> detail(@PathVariable Long id) {
        return Result.success(service.detail(id));
    }

    @PostMapping
    @RequiresPermission("emp:edit")
    public Result<?> add(@RequestBody HrEmployee e) {
        service.add(e);
        return Result.success("新增成功");
    }

    @PutMapping
    @RequiresPermission("emp:edit")
    public Result<?> update(@RequestBody HrEmployee e) {
        service.update(e);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("emp:edit")
    public Result<?> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.success("删除成功");
    }
}

