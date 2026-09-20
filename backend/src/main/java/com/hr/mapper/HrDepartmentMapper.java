package com.hr.mapper;
import com.hr.dto.HrDepartmentQueryDTO;
import com.hr.entity.HrDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HrDepartmentMapper {
    List<HrDepartment> selectPage(@Param("q") HrDepartmentQueryDTO q, @Param("offset") int offset);
    long selectCount(@Param("q") HrDepartmentQueryDTO q);
    HrDepartment selectById(@Param("id") Long id);
    List<HrDepartment> selectAll();   // 下拉框用，不分页
    int insert(HrDepartment d);
    int update(HrDepartment d);
    int logicDelete(@Param("id") Long id);
}

