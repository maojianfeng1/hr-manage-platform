package com.hr.mapper;
import com.hr.dto.HrSalaryStandardQueryDTO;
import com.hr.entity.HrSalaryStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface HrSalaryStandardMapper {
    List<HrSalaryStandard> selectPage(@Param("q") HrSalaryStandardQueryDTO q, @Param("offset") int offset);
    long selectCount(@Param("q") HrSalaryStandardQueryDTO q);
    HrSalaryStandard selectByEmployeeId(@Param("employeeId") Long employeeId);
    int insert(HrSalaryStandard s);
    int update(HrSalaryStandard s);
}

