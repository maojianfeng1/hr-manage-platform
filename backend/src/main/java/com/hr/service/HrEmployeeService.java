package com.hr.service;
import com.hr.common.result.PageResult;
import com.hr.dto.HrEmployeeQueryDTO;
import com.hr.entity.HrEmployee;

public interface HrEmployeeService {
    PageResult<HrEmployee> page(HrEmployeeQueryDTO q);
    HrEmployee detail(Long id);
    void add(HrEmployee e);
    void update(HrEmployee e);
    void remove(Long id);
}
