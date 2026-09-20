package com.hr.service;
import com.hr.common.result.PageResult;
import com.hr.dto.HrSalaryStandardQueryDTO;
import com.hr.entity.HrSalaryStandard;

public interface HrSalaryStandardService {
    PageResult<HrSalaryStandard> page(HrSalaryStandardQueryDTO q);
    HrSalaryStandard getByEmployeeId(Long employeeId);
    void save(HrSalaryStandard s);   // 存在则更新，不存在则插入
}

