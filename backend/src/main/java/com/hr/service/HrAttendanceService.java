package com.hr.service;
import com.hr.common.result.PageResult;
import com.hr.dto.HrAttendanceQueryDTO;
import com.hr.entity.HrAttendance;

public interface HrAttendanceService {
    PageResult<HrAttendance> page(HrAttendanceQueryDTO q);
    HrAttendance detail(Long id);
    void add(HrAttendance a);
    void update(HrAttendance a);
    void remove(Long id);
}
