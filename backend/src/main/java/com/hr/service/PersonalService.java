package com.hr.service;

import com.hr.entity.HrAttendance;
import com.hr.entity.HrEmployee;
import com.hr.entity.HrPayroll;

import java.util.List;

public interface PersonalService {

    /** 当前登录账号关联的员工档案 */
    HrEmployee myInfo();

    /** 当前员工的考勤（默认本月，可指定月份） */
    List<HrAttendance> myAttendance(String month);

    /** 当前员工的工资单 */
    List<HrPayroll> myPayroll();
}
