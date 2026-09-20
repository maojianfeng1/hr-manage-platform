package com.hr.dto;
import lombok.Data;
@Data
public class HrAttendanceQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private Long employeeId;
    private String empName;
    private Long deptId;
    private String status;
    private String month;       // 格式 yyyy-MM，如 2026-09
}
