package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.PageResult;
import com.hr.common.result.Result;
import com.hr.dto.HrSalaryStandardQueryDTO;
import com.hr.entity.HrSalaryStandard;
import com.hr.service.HrSalaryStandardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salary/standard")
public class HrSalaryStandardController {

    private final HrSalaryStandardService service;
    public HrSalaryStandardController(HrSalaryStandardService service) { this.service = service; }

    @GetMapping("/page")
    @RequiresPermission("sal:view")
    public Result<PageResult<HrSalaryStandard>> page(HrSalaryStandardQueryDTO q) {
        return Result.success(service.page(q));
    }

    @GetMapping("/{employeeId}")
    @RequiresPermission("sal:view")
    public Result<HrSalaryStandard> getByEmployeeId(@PathVariable Long employeeId) {
        return Result.success(service.getByEmployeeId(employeeId));
    }

    @PostMapping
    @RequiresPermission("sal:standard:edit")
    public Result<?> save(@RequestBody HrSalaryStandard s) {
        service.save(s);
        return Result.success("保存成功");
    }
}
