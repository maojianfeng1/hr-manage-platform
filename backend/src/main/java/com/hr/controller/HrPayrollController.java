package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.PageResult;
import com.hr.common.result.Result;
import com.hr.dto.HrPayrollQueryDTO;
import com.hr.entity.HrPayroll;
import com.hr.service.PayrollService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salary/payroll")
public class HrPayrollController {

    private final PayrollService service;
    public HrPayrollController(PayrollService service) { this.service = service; }

    @GetMapping("/page")
    @RequiresPermission("sal:view")
    public Result<PageResult<HrPayroll>> page(HrPayrollQueryDTO q) {
        return Result.success(service.page(q));
    }

    @GetMapping("/{id}")
    @RequiresPermission("sal:view")
    public Result<HrPayroll> detail(@PathVariable Long id) {
        return Result.success(service.detail(id));
    }

    /** 生成某月工资单：POST /api/salary/payroll/generate?month=2026-09 */
    @PostMapping("/generate")
    @RequiresPermission("sal:payroll:edit")
    public Result<?> generate(@RequestParam String month) {
        service.generate(month);
        return Result.success("算薪完成");
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("sal:payroll:edit")
    public Result<?> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.success("删除成功");
    }
}