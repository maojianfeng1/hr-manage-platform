package com.hr.dto;
import lombok.Data;
@Data
public class HrPayrollQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private Long employeeId;
    private String salaryMonth;
    private String empName;
    private Long deptId;
}
