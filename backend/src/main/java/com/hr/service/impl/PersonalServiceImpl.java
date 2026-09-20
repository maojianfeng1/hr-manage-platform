package com.hr.service.impl;

import com.hr.common.exception.BusinessException;
import com.hr.context.UserContext;
import com.hr.entity.HrAttendance;
import com.hr.entity.HrEmployee;
import com.hr.entity.HrPayroll;
import com.hr.mapper.PersonalMapper;
import com.hr.service.PersonalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class PersonalServiceImpl implements PersonalService {

    private final PersonalMapper personalMapper;

    public PersonalServiceImpl(PersonalMapper personalMapper) {
        this.personalMapper = personalMapper;
    }

    @Override
    public HrEmployee myInfo() {
        Long empId = currentEmpId();
        HrEmployee e = personalMapper.selectEmployeeById(empId);
        if (e == null) throw new BusinessException("未找到员工档案");
        return e;
    }

    @Override
    public List<HrAttendance> myAttendance(String month) {
        Long empId = currentEmpId();
        String m = (month == null || month.isBlank()) ? curMonth() : month;
        return personalMapper.selectAttendanceByEmpId(empId, m);
    }

    @Override
    public List<HrPayroll> myPayroll() {
        return personalMapper.selectPayrollByEmpId(currentEmpId());
    }

    /** 从 UserContext 取当前员工 id；未关联档案时抛业务异常 */
    private Long currentEmpId() {
        Long empId = UserContext.get() == null ? null : UserContext.get().getEmpId();
        if (empId == null) throw new BusinessException("当前账号未关联员工档案");
        return empId;
    }

    private String curMonth() {
        LocalDate d = LocalDate.now();
        return d.getYear() + "-" + String.format("%02d", d.getMonthValue());
    }
}
