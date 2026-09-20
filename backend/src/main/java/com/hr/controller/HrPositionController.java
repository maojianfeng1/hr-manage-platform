package com.hr.controller;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.PageResult;
import com.hr.common.result.Result;
import com.hr.dto.HrPositionQueryDTO;
import com.hr.entity.HrPosition;
import com.hr.service.HrPositionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/org/post")
public class HrPositionController {

    private final HrPositionService service;
    public HrPositionController(HrPositionService service) { this.service = service; }

    @GetMapping("/page")
    @RequiresPermission("org:post:view")
    public Result<PageResult<HrPosition>> page(HrPositionQueryDTO q) {
        return Result.success(service.page(q));
    }

    @GetMapping("/{id}")
    @RequiresPermission("org:post:view")
    public Result<HrPosition> detail(@PathVariable Long id) {
        return Result.success(service.detail(id));
    }

    @PostMapping
    @RequiresPermission("org:post:edit")
    public Result<?> add(@RequestBody HrPosition p) {
        service.add(p);
        return Result.success("新增成功");
    }

    @PutMapping
    @RequiresPermission("org:post:edit")
    public Result<?> update(@RequestBody HrPosition p) {
        service.update(p);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("org:post:edit")
    public Result<?> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.success("删除成功");
    }
}
