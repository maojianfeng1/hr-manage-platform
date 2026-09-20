package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.PageResult;
import com.hr.common.result.Result;
import com.hr.dto.HrDepartmentQueryDTO;
import com.hr.entity.HrDepartment;
import com.hr.service.HrDepartmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org/dept")
public class HrDepartmentController {

    private final HrDepartmentService service;

    public HrDepartmentController(HrDepartmentService service) {
        this.service = service;
    }

    @GetMapping("/page")
    @RequiresPermission("org:dept:view")
    public Result<PageResult<HrDepartment>> page(@Valid HrDepartmentQueryDTO q) {
        return Result.success(service.page(q));
    }

    @GetMapping("/list")
    @RequiresPermission("org:dept:view")
    public Result<List<HrDepartment>>list(){
        return Result.success(service.listAll());
    }

    @GetMapping("/{id}")
    @RequiresPermission("org:dept:view")
    public Result<HrDepartment>detail(@PathVariable Long id){
        return Result.success(service.detail(id));
    }

    @PostMapping
    @RequiresPermission("org:dept:edit")
    public Result<?>add(@RequestBody HrDepartment d){
        service.add(d);
        return  Result.success("新增成功");
    }

    @PutMapping
    @RequiresPermission("org:dept:edit")
    public Result<?>update(@RequestBody HrDepartment d){
        service.update(d);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("org:dept:edit")
    public Result<?>remove(@PathVariable Long id){
        service.remove(id);
        return Result.success("删除成功");
    }
}

