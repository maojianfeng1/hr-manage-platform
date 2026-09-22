package com.hr.mapper;
import com.hr.dto.HrAttendanceQueryDTO;
import com.hr.entity.HrAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface HrAttendanceMapper {
    List<HrAttendance> selectPage(@Param("q") HrAttendanceQueryDTO q, @Param("offset") int offset);
    long selectCount(@Param("q") HrAttendanceQueryDTO q);
    HrAttendance selectById(@Param("id") Long id);
    int insert(HrAttendance a);
    int update(HrAttendance a);
    int logicDelete(@Param("id") Long id);

    /** 员工改名时级联刷新历史考勤记录中的姓名快照 */
    int updateEmpNameByEmployeeId(@Param("employeeId") Long employeeId, @Param("empName") String empName);
}
