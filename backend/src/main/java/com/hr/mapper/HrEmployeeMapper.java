package com.hr.mapper;
import com.hr.dto.HrEmployeeQueryDTO;
import com.hr.entity.HrEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface HrEmployeeMapper {
    List<HrEmployee> selectPage(@Param("q") HrEmployeeQueryDTO q, @Param("offset") int offset);
    long selectCount(@Param("q") HrEmployeeQueryDTO q);
    HrEmployee selectById(@Param("id") Long id);
    int insert(HrEmployee e);
    int update(HrEmployee e);
    int logicDelete(@Param("id") Long id);

    List<HrEmployee> selectByStatus(@Param("status") int status);
}
