package com.hr.service;
import com.hr.common.result.PageResult;
import com.hr.dto.HrPayrollQueryDTO;
import com.hr.entity.HrPayroll;

public interface PayrollService {
    PageResult<HrPayroll> page(HrPayrollQueryDTO q);
    HrPayroll detail(Long id);
    void generate(String salaryMonth);
    void remove(Long id);
}

