package com.hr.mapper;
import com.hr.dto.HrPayrollQueryDTO;
import com.hr.entity.HrPayroll;
import com.hr.vo.AttendanceSummaryVO;
import com.hr.vo.PayrollSourceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HrPayrollMapper {
    /** 取员工姓名/部门/岗位快照（算薪时写进工资单） */
    PayrollSourceVO selectSource(@Param("employeeId") Long employeeId);

    /** 汇总某员工某月的考勤：加班小时、迟到/早退/旷工/事假/病假次数 */
    AttendanceSummaryVO selectAttendanceSummary(@Param("employeeId") Long employeeId, @Param("month") String month);

    List<HrPayroll> selectPage(@Param("q") HrPayrollQueryDTO q, @Param("offset") int offset);
    long selectCount(@Param("q") HrPayrollQueryDTO q);

    /** 按「员工+月份」查，用于算薪幂等（重复生成不产生两条） */
    HrPayroll selectByEmpMonth(@Param("employeeId") Long employeeId, @Param("salaryMonth") String month);
    HrPayroll selectById(@Param("id") Long id);

    int insert(HrPayroll p);
    int update(HrPayroll p);
    int logicDelete(@Param("id") Long id);

    /** 员工改名时级联刷新历史工资单中的姓名快照 */
    int updateEmpNameByEmployeeId(@Param("employeeId") Long employeeId, @Param("empName") String empName);
}
