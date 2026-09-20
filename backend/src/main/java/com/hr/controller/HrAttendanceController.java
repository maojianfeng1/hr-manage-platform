package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.PageResult;
import com.hr.common.result.Result;
import com.hr.dto.HrAttendanceQueryDTO;
import com.hr.entity.HrAttendance;
import com.hr.service.HrAttendanceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
public class HrAttendanceController {

    private final HrAttendanceService service;
    public HrAttendanceController(HrAttendanceService service) { this.service = service; }

    @GetMapping("/page")
    @RequiresPermission("att:view")
    public Result<PageResult<HrAttendance>> page(HrAttendanceQueryDTO q) {
        return Result.success(service.page(q));
    }

    @GetMapping("/{id}")
    @RequiresPermission("att:view")
    public Result<HrAttendance> detail(@PathVariable Long id) {
        return Result.success(service.detail(id));
    }

    @PostMapping
    @RequiresPermission("att:edit")
    public Result<?> add(@RequestBody HrAttendance a) {
        service.add(a);
        return Result.success("新增成功");
    }

    @PutMapping
    @RequiresPermission("att:edit")
    public Result<?> update(@RequestBody HrAttendance a) {
        service.update(a);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("att:edit")
    public Result<?> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.success("删除成功");
    }
}

