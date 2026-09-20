package com.hr.mapper;

import com.hr.entity.HrAttendance;
import com.hr.entity.HrEmployee;
import com.hr.entity.HrPayroll;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PersonalMapper {

    /** 按员工 id 查档案 */
    HrEmployee selectEmployeeById(@Param("id") Long id);

    /** 按员工 id + 月份（YYYY-MM）查考勤 */
    List<HrAttendance> selectAttendanceByEmpId(@Param("empId") Long empId, @Param("month") String month);

    /** 按员工 id 查工资单（倒序，最新在前） */
    List<HrPayroll> selectPayrollByEmpId(@Param("empId") Long empId);
}
