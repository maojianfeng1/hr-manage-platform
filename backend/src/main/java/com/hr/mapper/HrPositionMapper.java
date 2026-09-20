package com.hr.mapper;
import com.hr.dto.HrPositionQueryDTO;
import com.hr.entity.HrPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface HrPositionMapper {
    List<HrPosition> selectPage(@Param("q") HrPositionQueryDTO q, @Param("offset") int offset);
    long selectCount(@Param("q") HrPositionQueryDTO q);
    HrPosition selectById(@Param("id") Long id);
    int insert(HrPosition d);
    int update(HrPosition d);
    int logicDelete(@Param("id") Long id);
}

