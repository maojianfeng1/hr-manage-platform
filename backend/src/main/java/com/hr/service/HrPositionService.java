package com.hr.service;
import com.hr.common.result.PageResult;
import com.hr.dto.HrPositionQueryDTO;
import com.hr.entity.HrPosition;

public interface HrPositionService {
    PageResult<HrPosition> page(HrPositionQueryDTO q);
    HrPosition detail(Long id);
    void add(HrPosition p);
    void update(HrPosition p);
    void remove(Long id);
}
