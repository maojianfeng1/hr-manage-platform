package com.hr.controller;

import com.hr.common.result.Result;
import com.hr.entity.HrAttendance;
import com.hr.entity.HrEmployee;
import com.hr.entity.HrPayroll;
import com.hr.service.PersonalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {

    private final PersonalService personalService;

    public PersonalController(PersonalService personalService) {
        this.personalService = personalService;
    }

    /**
     * 我的员工档案。
     * 注意：不加 @RequiresPermission —— 这是「个人自助」接口，只要登录就能看「自己」的数据，
     * 权限由 UserContext 里的 empId（行级）保证，不会越权看到别人。
     */
    @GetMapping("/my-info")
    public Result<HrEmployee> myInfo() {
        return Result.success(personalService.myInfo());
    }

    /** 我的考勤，默认本月，可 ?month=YYYY-MM 指定 */
    @GetMapping("/my-attendance")
    public Result<List<HrAttendance>> myAttendance(@RequestParam(required = false) String month) {
        return Result.success(personalService.myAttendance(month));
    }

    /** 我的工资单，按月份倒序 */
    @GetMapping("/my-payroll")
    public Result<List<HrPayroll>> myPayroll() {
        return Result.success(personalService.myPayroll());
    }
}
