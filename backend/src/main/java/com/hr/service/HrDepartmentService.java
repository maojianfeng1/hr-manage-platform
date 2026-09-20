package com.hr.service;
import com.hr.common.result.PageResult;
import com.hr.dto.HrDepartmentQueryDTO;
import com.hr.entity.HrDepartment;
import java.util.List;

public interface HrDepartmentService {
    PageResult<HrDepartment> page(HrDepartmentQueryDTO q);
    List<HrDepartment> listAll();
    HrDepartment detail(Long id);
    void add(HrDepartment d);
    void update(HrDepartment d);
    void remove(Long id);
}

